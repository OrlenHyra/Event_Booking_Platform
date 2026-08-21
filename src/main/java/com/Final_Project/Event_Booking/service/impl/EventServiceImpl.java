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
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
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

        } else if (event.getStatus() == EventStatus.ACTIVE
                && !now.isBefore(event.getEndDateTime())) {

            event.setStatus(EventStatus.COMPLETED);
            eventRepository.save(event);
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

        return eventMapper.toResponseDTO(savedEvent);
    }

    @Override
    public EventResponseDTO publishEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(()->new ResourcesNotFoundException("Event with id:"+id+" is not found!"));
        User user = userService.getCurrentUser();

        if(user.getRole()==UserRole.ORGANIZER && !userService.isCurrentUserOwner(event)){
            throw new UnauthorizedAccessException("You are not allowed to publish this event!");
        }
        if(event.getStatus()!=EventStatus.DRAFT){
            throw new BusinessRuleException("Only Draft events can be published!");
        }
        event.setStatus(EventStatus.UPCOMING);
        Event publishedEvent=eventRepository.save(event);
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
        return eventRepository.findByOrganizer_Id(
                user.getId()
        )
                .stream()
                .map(event -> {
                    EventResponseDTO response=eventMapper.toResponseDTO(event);
                    response.setAverageRating(calculateAverageRating(event));
                return response;
                })
                .toList();
    }

    @Override
    public List<EventResponseDTO> getAllEventsForAdmin() {
        return eventRepository.findAll()
                .stream()
                .map(event -> {
                    EventResponseDTO response=eventMapper.toResponseDTO(event);
                    response.setAverageRating(calculateAverageRating(event));
                    return response;
                })
                .toList();
    }

    @Override
    public List<EventResponseDTO> getAllEvents() {
        return eventRepository.findByStatusIn(
                List.of(
                        EventStatus.ACTIVE,
                        EventStatus.UPCOMING,
                        EventStatus.COMPLETED
                )
                )
                .stream()
                .map(event ->{
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
        Event event = eventRepository.findById(id)
                .orElseThrow(()-> new ResourcesNotFoundException("Event with id:"+id+" is not found!"));

        User currentUser = userService.getCurrentUser();

        if (currentUser.getRole() == UserRole.ORGANIZER && !userService.isCurrentUserOwner(event)) {
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

        return eventMapper.toResponseDTO(updatedEvent);
    }

    @Override
    public EventResponseDTO cancelEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourcesNotFoundException("Event with id:"+id+" is not found!"));
        if (!userService.isCurrentUserOwner(event)) {
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
        return eventMapper.toResponseDTO(cancelledEvent);
    }

    @Override
    public Page<EventResponseDTO> getEventsByCategory(String categoryName, Pageable pageable) {
        Page<Event> events=eventRepository.findByStatusInAndCategories_Name(
                List.of(
                        EventStatus.ACTIVE,
                        EventStatus.COMPLETED,
                        EventStatus.UPCOMING
                ),
                categoryName,
                pageable
        );
        return events.map(eventMapper::toResponseDTO);
    }

    @Override
    public Page<EventResponseDTO> getEventsByCity(String cityName,Pageable pageable) {
        Page<Event> events=eventRepository.findByStatusInAndVenue_City(
                List.of(
                        EventStatus.ACTIVE,
                        EventStatus.COMPLETED,
                        EventStatus.UPCOMING
                ),
                cityName,
                pageable
        );
        return events.map(eventMapper::toResponseDTO);
    }

    @Override
    public Page<EventResponseDTO> filterByDateRange(LocalDateTime startDateTime, LocalDateTime endDateTime,Pageable pageable) {
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
        return events.map(eventMapper::toResponseDTO);
    }

    @Override
    public Page<EventResponseDTO> filterByPriceRange(BigDecimal minPrice, BigDecimal maxPrice,Pageable pageable) {
        Page<Event> events =eventRepository.findEventByPriceRange(
                minPrice,maxPrice,pageable
        );
        return events.map(eventMapper::toResponseDTO);
    }
}
