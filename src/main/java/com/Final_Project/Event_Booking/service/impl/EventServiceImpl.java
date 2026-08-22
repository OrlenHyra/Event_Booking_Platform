package com.Final_Project.Event_Booking.service.impl;

import com.Final_Project.Event_Booking.exception.custom.BusinessRuleException;
import com.Final_Project.Event_Booking.exception.custom.ResourcesNotFoundException;
import com.Final_Project.Event_Booking.exception.custom.UnauthorizedAccessException;
import com.Final_Project.Event_Booking.model.dto.request.EventRequestDTO;
import com.Final_Project.Event_Booking.model.dto.response.EventResponseDTO;
import com.Final_Project.Event_Booking.model.entity.*;
import com.Final_Project.Event_Booking.model.enums.EventStatus;
import com.Final_Project.Event_Booking.model.enums.UserRole;
import com.Final_Project.Event_Booking.model.mapper.EventMapper;
import com.Final_Project.Event_Booking.repository.CategoryRepository;
import com.Final_Project.Event_Booking.repository.EventRepository;
import com.Final_Project.Event_Booking.repository.VenueRepository;
import com.Final_Project.Event_Booking.service.EventService;
import com.Final_Project.Event_Booking.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    private final VenueRepository venueRepository;
    private final CategoryRepository categoryRepository;
    private final UserService userService;


    private Double calculateAverageRating(Event event){
        if(event.getReviews() == null || event.getReviews().isEmpty()){
            return null;
        }
        return event.getReviews()
                .stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

    private void updateEventStatus(Event event) {

        LocalDateTime now = LocalDateTime.now();

        if (event.getStatus() == EventStatus.UPCOMING
                && !now.isBefore(event.getStartDateTime())) {

            event.setStatus(EventStatus.ACTIVE);
            eventRepository.save(event);
            log.info("Event with id: {} changed status from UPCOMING to ACTIVE",
                    event.getId());

        } else if (event.getStatus() == EventStatus.ACTIVE
                && !now.isBefore(event.getEndDateTime())) {

            event.setStatus(EventStatus.COMPLETED);
            eventRepository.save(event);
            log.info("Event with id: {} changed status from ACTIVE to COMPLETED",
                    event.getId());
        }
    }

    private void checkVenueAvailability(Venue venue, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        Long overlappingEventId = eventRepository.findOverlappingEventId(
                venue.getId(),
                startDateTime,
                endDateTime
        );
        if (overlappingEventId != null) {
            throw new BusinessRuleException("The venue already has another event during the selected time!");
        }
    }
    @Override
    public EventResponseDTO createEvent(EventRequestDTO request) {
        User organizer = userService.getCurrentUser();
        log.info("Creating event for organizer with id: {}",
                organizer.getId());

        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(()-> new ResourcesNotFoundException("Venue with id:"+request.getVenueId()+" is not found!"));

        if (!request.getStartDateTime().isBefore(request.getEndDateTime())) {
            throw new BusinessRuleException("Start date and time must be before the end date and time!");
        }

        if (request.getTotalSeats() > venue.getCapacity()) {
            throw new BusinessRuleException("Total seats cannot exceed the venue capacity!");
        }

        checkVenueAvailability(venue, request.getStartDateTime(), request.getEndDateTime());

        List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());

        if (categories.size() != request.getCategoryIds().size()) {
            throw new ResourcesNotFoundException("One or more categories were not found!");
        }

        Event event = eventMapper.toEntity(request);

        event.setOrganizer(organizer);
        event.setVenue(venue);
        event.setCategories(categories);
        event.setAvailableSeats(request.getTotalSeats());
        event.setStatus(EventStatus.DRAFT);

        Event savedEvent = eventRepository.save(event);
        log.info("Event created successfully with id: {} by organizer: {}",
                savedEvent.getId(), organizer.getId());

        return eventMapper.toResponseDTO(savedEvent);
    }

    @Override
    public EventResponseDTO publishEvent(Long id) {
        log.info("Attempting to publish event with id: {}", id);
        Event event = eventRepository.findById(id)
                .orElseThrow(()->new ResourcesNotFoundException("Event with id:"+id+" is not found!"));
        User user = userService.getCurrentUser();

        if(user.getRole()==UserRole.ORGANIZER && !userService.isCurrentUserOwner(event)){
            log.warn("User with id: {} attempted to publish event {} without permission",
                    user.getId(), id);
            throw new UnauthorizedAccessException("You are not allowed to publish this event!");
        }
        if(event.getStatus()!=EventStatus.DRAFT){
            throw new BusinessRuleException("Only Draft events can be published!");
        }
        event.setStatus(EventStatus.UPCOMING);
        Event publishedEvent=eventRepository.save(event);
        log.info("Event with id: {} published successfully by user: {}",
                id, user.getId());
        return eventMapper.toResponseDTO(publishedEvent);
    }

    @Override
    public EventResponseDTO getEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourcesNotFoundException("Event with id:"+id+" is not found!"));
        updateEventStatus(event);
        if (!List.of(
                EventStatus.UPCOMING,
                EventStatus.ACTIVE,
                EventStatus.COMPLETED
        ).contains(event.getStatus())) {
            throw new ResourcesNotFoundException("Event with id:"+id+" is not available!");
        }
        EventResponseDTO response = eventMapper.toResponseDTO(event);
        response.setAverageRating(calculateAverageRating(event));
        return response;
    }

    @Override
    public List<EventResponseDTO> getOrganizerEvents() {
        User user=userService.getCurrentUser();
        log.info("Fetching events for organizer with id: {}", user.getId());
        return eventRepository.findByOrganizer_Id(
                user.getId()
        )
                .stream()
                .map(event -> {
                    updateEventStatus(event);
                    EventResponseDTO response=eventMapper.toResponseDTO(event);
                    response.setAverageRating(calculateAverageRating(event));
                return response;
                })
                .toList();
    }

    @Override
    public List<EventResponseDTO> getAllEventsForAdmin() {
        log.info("Admin requested all events");
        return eventRepository.findAll()
                .stream()
                .map(event -> {
                    updateEventStatus(event);
                    EventResponseDTO response=eventMapper.toResponseDTO(event);
                    response.setAverageRating(calculateAverageRating(event));
                    return response;
                })
                .toList();
    }

    @Override
    public List<EventResponseDTO> getAllEvents() {
        log.info("Fetching all publicly available events");
        return eventRepository.findByStatusIn(
                List.of(
                        EventStatus.ACTIVE,
                        EventStatus.UPCOMING,
                        EventStatus.COMPLETED
                )
                )
                .stream()
                .map(event ->{
                    updateEventStatus(event);
                    EventResponseDTO response=eventMapper.toResponseDTO(event);
                    response.setAverageRating(calculateAverageRating(event));
                    return response;
        })
                .toList();
    }

    private void checkVenueAvailabilityForUpdate(Long eventId, Venue venue, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        Long overlappingEventId = eventRepository.findOverlappingEventIdForUpdate(
                eventId,
                venue.getId(),
                startDateTime,
                endDateTime
        );
        if (overlappingEventId != null) {
            throw new BusinessRuleException("The venue already has another event during the selected time!");
        }
    }

    @Override
    public EventResponseDTO updateEvent(Long id, EventRequestDTO request) {
        log.info("Attempting to update event with id: {}", id);
        Event event = eventRepository.findById(id)
                .orElseThrow(()-> new ResourcesNotFoundException("Event with id:"+id+" is not found!"));

        User currentUser = userService.getCurrentUser();

        if (currentUser.getRole() == UserRole.ORGANIZER && !userService.isCurrentUserOwner(event)) {
            log.warn("User with id: {} attempted to update event {} without permission",
                    currentUser.getId(), id);
            throw new UnauthorizedAccessException("You are not allowed to update this event!");
        }

        if (event.getStatus() != EventStatus.DRAFT && event.getStatus() != EventStatus.UPCOMING) {
            throw new BusinessRuleException("Only draft or upcoming events can be updated!");
        }

        if (!request.getStartDateTime().isBefore(request.getEndDateTime())) {
            throw new BusinessRuleException("Start date and time must be before the end date and time!");
        }

        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(()-> new ResourcesNotFoundException("Venue with id:"+request.getVenueId()+" is not found!"));

        if (request.getTotalSeats() > venue.getCapacity()) {
            throw new BusinessRuleException("Total seats cannot exceed the venue capacity!");
        }

        checkVenueAvailabilityForUpdate(event.getId(), venue, request.getStartDateTime(), request.getEndDateTime());

        List<Category> categories=categoryRepository.findAllById(request.getCategoryIds());

        if (categories.size() != request.getCategoryIds().size()) {
            throw new ResourcesNotFoundException("One or more categories were not found!");
        }

        eventMapper.updateEntity(request, event);
        event.setVenue(venue);
        event.setCategories(categories);

        Event updatedEvent = eventRepository.save(event);
        log.info("Event with id: {} updated successfully by user: {}",
                id, currentUser.getId());
        return eventMapper.toResponseDTO(updatedEvent);
    }

    @Override
    public EventResponseDTO cancelEvent(Long id) {
        log.info("Attempting to cancel event with id: {}", id);
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourcesNotFoundException("Event with id:"+id+" is not found!"));
        User currentUser = userService.getCurrentUser();
        if (!userService.isCurrentUserOwner(event)) {
            log.warn("User with id: {} attempted to cancel event {} without permission",
                    currentUser.getId(), id);
            throw new UnauthorizedAccessException("You are not allowed to cancel this event!");
        }
        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new BusinessRuleException("Event is already cancelled!");
        }
        if (event.getStatus() == EventStatus.COMPLETED) {
            throw new BusinessRuleException("Completed events cannot be cancelled!");
        }
        event.setStatus(EventStatus.CANCELLED);
        Event cancelledEvent=eventRepository.save(event);
        log.info("Event with id: {} cancelled successfully by user: {}",
                id, currentUser.getId());
        return eventMapper.toResponseDTO(cancelledEvent);
    }

    @Override
    public Page<EventResponseDTO> getEventsByCategory(String categoryName, Pageable pageable) {
        log.info("Searching events by category: {}", categoryName);
        Page<Event> events=eventRepository.findByStatusInAndCategories_Name(
                List.of(
                        EventStatus.ACTIVE,
                        EventStatus.COMPLETED,
                        EventStatus.UPCOMING
                ),
                categoryName,
                pageable
        );
        log.info("Found {} events for category: {}",
                events.getTotalElements(), categoryName);
        return events.map(eventMapper::toResponseDTO);
    }

    @Override
    public Page<EventResponseDTO> getEventsByCity(String cityName,Pageable pageable) {
        log.info("Searching events by city: {}", cityName);
        Page<Event> events=eventRepository.findByStatusInAndVenue_City(
                List.of(
                        EventStatus.ACTIVE,
                        EventStatus.COMPLETED,
                        EventStatus.UPCOMING
                ),
                cityName,
                pageable
        );
        log.info("Found {} events for city: {}",
                events.getTotalElements(), cityName);
        return events.map(eventMapper::toResponseDTO);
    }

    @Override
    public Page<EventResponseDTO> filterByDateRange(LocalDateTime startDateTime, LocalDateTime endDateTime,Pageable pageable) {
        log.info("Filtering events by date range: {} to {}",
                startDateTime, endDateTime);
        Page<Event> events=eventRepository.findEventByDateRange(
                List.of(
                        EventStatus.ACTIVE,
                        EventStatus.COMPLETED,
                        EventStatus.UPCOMING
                ),
                startDateTime,
                endDateTime,
                pageable
        );
        log.info("Found {} events in requested date range",
                events.getTotalElements());
        return events.map(eventMapper::toResponseDTO);
    }

    @Override
    public Page<EventResponseDTO> filterByPriceRange(BigDecimal minPrice, BigDecimal maxPrice,Pageable pageable) {
        log.info("Filtering events by price range: {} to {}",
                minPrice, maxPrice);
        Page<Event> events =eventRepository.findEventByPriceRange(
                minPrice,maxPrice,pageable
        );
        log.info("Found {} events in requested price range",
                events.getTotalElements());
        return events.map(eventMapper::toResponseDTO);
    }
}
