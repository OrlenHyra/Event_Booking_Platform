package com.Final_Project.Event_Booking.service.impl;

import com.Final_Project.Event_Booking.exception.custom.ResourcesNotFoundException;
import com.Final_Project.Event_Booking.model.dto.request.EventRequestDTO;
import com.Final_Project.Event_Booking.model.dto.response.EventResponseDTO;
import com.Final_Project.Event_Booking.model.entity.Category;
import com.Final_Project.Event_Booking.model.entity.Event;
import com.Final_Project.Event_Booking.model.entity.User;
import com.Final_Project.Event_Booking.model.entity.Venue;
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

import org.springframework.security.access.AccessDeniedException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    private final VenueRepository venueRepository;
    private final CategoryRepository categoryRepository;
    private final UserService userService;

    @Override
    public EventResponseDTO createEvent(EventRequestDTO request) {
        User organizer = userService.getCurrentUser();

        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(()-> new ResourcesNotFoundException("Venue with id:"+request.getVenueId()+" is not found!"));

        if (request.getTotalSeats() > venue.getCapacity()) {
            throw new IllegalArgumentException("Total seats cannot exceed the venue capacity!");
        }

        if (!request.getStartDateTime().isBefore(request.getEndDateTime())) {
            throw new IllegalArgumentException("Start date and time must be before the end date and time!");
        }

        List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());

        if (categories.size() != request.getCategoryIds().size()) {
            throw new ResourcesNotFoundException("One or more categories were not found!");
        }

        Event event = eventMapper.toEntity(request);

        event.setOrganizer(organizer);
        event.setVenue(venue);
        event.setCategories(categories);
        event.setAvailableSeats(request.getTotalSeats());
        event.setStatus(EventStatus.UPCOMING);

        Event savedEvent = eventRepository.save(event);

        return eventMapper.toResponseDTO(savedEvent);
    }

    @Override
    public EventResponseDTO getEvent(Long id) {
        Event event=eventRepository.findById(id)
                .orElseThrow(()->new ResourcesNotFoundException("Event with id:"+id+" is not found!"));
        return eventMapper.toResponseDTO(event);
    }

    @Override
    public List<EventResponseDTO> getAllEvents() {
        return eventRepository.findAll()
                .stream()
                .map(eventMapper::toResponseDTO)
                .toList();
    }

    @Override
    public EventResponseDTO updateEvent(Long id, EventRequestDTO request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(()-> new ResourcesNotFoundException("Event with id:"+id+" is not found!"));

        User currentUser = userService.getCurrentUser();

        if (currentUser.getRole() == UserRole.ORGANIZER && !userService.isCurrentUserOwner(event)) {
            throw new AccessDeniedException("You are not allowed to update this event!");
        }

        if (!request.getStartDateTime().isBefore(request.getEndDateTime())) {
            throw new IllegalArgumentException("Start date and time must be before the end date and time!");
        }

        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(()-> new ResourcesNotFoundException("Venue with id:"+request.getVenueId()+" is not found!"));

        if (request.getTotalSeats() > venue.getCapacity()) {
            throw new IllegalArgumentException("Total seats cannot exceed the venue capacity!");
        }

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
    public void deleteEvent(Long id) {
        Event event=eventRepository.findById(id)
                .orElseThrow(()->new ResourcesNotFoundException("Event with id:"+id+" is not found!"));
        eventRepository.delete(event);
    }
}
