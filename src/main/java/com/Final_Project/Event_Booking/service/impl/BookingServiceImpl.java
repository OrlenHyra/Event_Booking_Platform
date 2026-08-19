package com.Final_Project.Event_Booking.service.impl;

import com.Final_Project.Event_Booking.exception.custom.BusinessRuleException;
import com.Final_Project.Event_Booking.exception.custom.ResourcesNotFoundException;
import com.Final_Project.Event_Booking.model.dto.request.BookingRequestDTO;
import com.Final_Project.Event_Booking.model.dto.response.BookingResponseDTO;
import com.Final_Project.Event_Booking.model.entity.Booking;
import com.Final_Project.Event_Booking.model.entity.Event;
import com.Final_Project.Event_Booking.model.entity.User;
import com.Final_Project.Event_Booking.model.enums.BookingStatus;
import com.Final_Project.Event_Booking.model.enums.EventStatus;
import com.Final_Project.Event_Booking.model.mapper.BookingMapper;
import com.Final_Project.Event_Booking.repository.BookingRepository;
import com.Final_Project.Event_Booking.repository.EventRepository;
import com.Final_Project.Event_Booking.service.BookingService;
import com.Final_Project.Event_Booking.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;

    private final UserService userService;
    private final EventRepository eventRepository;

    @Transactional
    @Override
    public BookingResponseDTO createBooking(BookingRequestDTO request) {
        User user=userService.getCurrentUser();
        Event event=eventRepository.findById(request.getEventId())
                .orElseThrow(()->new ResourcesNotFoundException(("Event with id:"+request.getEventId()+" is not found!")));
        if (event.getStatus() != EventStatus.UPCOMING) {
            throw new BusinessRuleException("Bookings are only allowed for upcoming events!");
        }

        if (!LocalDateTime.now().isBefore(event.getStartDateTime())) {
            throw new BusinessRuleException("The event has already started!");
        }

        if (request.getSeatsBooked() > event.getAvailableSeats()) {
            throw new BusinessRuleException("Not enough seats available for this event!");
        }
        event.setAvailableSeats(event.getAvailableSeats() - request.getSeatsBooked());
        Booking booking=bookingMapper.toEntity(request);
        booking.setBooker(user);
        booking.setEvent(event);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setBookingDate(LocalDateTime.now());

        Booking savedBooking=bookingRepository.save(booking);

        return bookingMapper.toResponseDTO(savedBooking);
    }

    @Override
    public BookingResponseDTO getBooking(Long id) {
        Booking booking=bookingRepository.findById(id)
                .orElseThrow(()->new ResourcesNotFoundException("Booking with id:"+id+" is not found!"));
        return bookingMapper.toResponseDTO(booking);
    }

    @Override
    public List<BookingResponseDTO> getAllBookings() {
        return bookingRepository.findAll()
                .stream()
                .map(bookingMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    @Override
    public BookingResponseDTO updateBooking(Long id, BookingRequestDTO request){
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourcesNotFoundException("Booking with id:"+id+" is not found!"));
        User user = userService.getCurrentUser();
        if (!booking.getBooker().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not allowed to modify this booking!");
        }

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessRuleException("Only confirmed bookings can be updated!");
        }

        Event event = booking.getEvent();
        if (event.getStatus() != EventStatus.UPCOMING) {
            throw new BusinessRuleException("Bookings can only be modified for upcoming events!");
        }

        if (!LocalDateTime.now().isBefore(event.getStartDateTime())) {
            throw new BusinessRuleException("The event has already started!");
        }

        int oldSeats = booking.getSeatsBooked();
        int newSeats = request.getSeatsBooked();
        int seatDifference = newSeats - oldSeats;

        if (seatDifference > 0 && seatDifference > event.getAvailableSeats()) {
            throw new BusinessRuleException("Not enough seats available for this event!"
            );
        }
        event.setAvailableSeats(event.getAvailableSeats() - seatDifference);
        booking.setSeatsBooked(newSeats);
        eventRepository.save(event);
        Booking updatedBooking = bookingRepository.save(booking);

        return bookingMapper.toResponseDTO(updatedBooking);
    }

    @Override
    public void deleteBooking(Long id) {
        Booking booking=bookingRepository.findById(id)
                .orElseThrow(()->new ResourcesNotFoundException("Booking with id:"+id+" is not found!"));
        bookingRepository.delete(booking);
    }
}
