package com.Final_Project.Event_Booking.service.impl;

import com.Final_Project.Event_Booking.exception.custom.BusinessRuleException;
import com.Final_Project.Event_Booking.exception.custom.ResourcesNotFoundException;
import com.Final_Project.Event_Booking.exception.custom.UnauthorizedAccessException;
import com.Final_Project.Event_Booking.model.dto.request.BookingRequestDTO;
import com.Final_Project.Event_Booking.model.dto.response.BookingCreationResponseDTO;
import com.Final_Project.Event_Booking.model.dto.response.BookingResponseDTO;
import com.Final_Project.Event_Booking.model.entity.Booking;
import com.Final_Project.Event_Booking.model.entity.Event;
import com.Final_Project.Event_Booking.model.entity.User;
import com.Final_Project.Event_Booking.model.enums.BookingStatus;
import com.Final_Project.Event_Booking.model.enums.EventStatus;
import com.Final_Project.Event_Booking.model.enums.UserRole;
import com.Final_Project.Event_Booking.model.mapper.BookingMapper;
import com.Final_Project.Event_Booking.repository.BookingRepository;
import com.Final_Project.Event_Booking.repository.EventRepository;
import com.Final_Project.Event_Booking.service.BookingService;
import com.Final_Project.Event_Booking.service.UserService;
import com.Final_Project.Event_Booking.service.WaitlistService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;

    private final UserService userService;
    private final EventRepository eventRepository;
    private final WaitlistService waitlistService;

    private BookingCreationResponseDTO createConfirmedBooking(BookingRequestDTO request, User user, Event event) {
        event.setAvailableSeats(event.getAvailableSeats() - request.getSeatsBooked());
        eventRepository.save(event);

        Booking booking = bookingMapper.toEntity(request);
        booking.setBooker(user);
        booking.setEvent(event);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setBookingDate(LocalDateTime.now());

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking created successfully. Booking id: {}, user id: {}, event id: {}, seats booked: {}",
                savedBooking.getId(), user.getId(), event.getId(), request.getSeatsBooked());
        BookingResponseDTO response = bookingMapper.toResponseDTO(savedBooking);
        response.setMessage("Booking created successfully.");

        return BookingCreationResponseDTO.builder()
                .booking(response)
                .build();
    }

    private void validateEventForBooking(Event event) {
        if (event.getStatus() != EventStatus.UPCOMING) {
            throw new BusinessRuleException("Bookings are only allowed for upcoming events!");
        }
        if (!LocalDateTime.now().isBefore(event.getStartDateTime())) {
            throw new BusinessRuleException("The event has already started!");
        }
    }

    @Transactional
    @Override
    public BookingCreationResponseDTO createBooking(BookingRequestDTO request) {
        User user = userService.getCurrentUser();
        log.info("User id: {} attempting to book event id: {} for {} seats",
                user.getId(), request.getEventId(), request.getSeatsBooked());
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new ResourcesNotFoundException("Event with id:"+request.getEventId()+" is not found!"));
        validateEventForBooking(event);
        if (request.getSeatsBooked() <= event.getAvailableSeats()) {
            return createConfirmedBooking(request, user, event);
        }
        log.warn("Insufficient seats for event id: {}. User id: {} requested: {}, available: {}. Adding to waitlist",
                event.getId(), user.getId(), request.getSeatsBooked(), event.getAvailableSeats());
        return waitlistService.createWaitlist(user, event, request.getSeatsBooked());
    }

    @Override
    public BookingResponseDTO getBooking(Long id) {
        Booking booking=bookingRepository.findById(id)
                .orElseThrow(()->new ResourcesNotFoundException("Booking with id:"+id+" is not found!"));
        User user = userService.getCurrentUser();
        if (user.getRole() == UserRole.ATTENDEE && !booking.getBooker().getId().equals(user.getId())) {
            log.warn("User id: {} attempted to access booking id: {} without permission",
                    user.getId(), id);
            throw new UnauthorizedAccessException("You are not allowed to view this booking!");
        }
        return bookingMapper.toResponseDTO(booking);
    }

    @Override
    public List<BookingResponseDTO> getAllBookings() {
        log.info("Fetching all bookings");
        User user=userService.getCurrentUser();
        return bookingRepository.findAll()
                .stream()
                .map(bookingMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public void cancelBooking(Long id) {
        log.info("Admin attempting to cancel booking id: {}", id);
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourcesNotFoundException("Booking with id: " + id + " is not found!"));
        User user = userService.getCurrentUser();
        if (user.getRole() != UserRole.ADMIN) {
            log.warn("User id: {} attempted to cancel booking id: {} without admin privileges",
                    user.getId(), id);
            throw new UnauthorizedAccessException("Only admins are allowed to cancel bookings!");
        }
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessRuleException("Only confirmed bookings can be cancelled!");
        }
        performCancelBooking(booking);
        log.info("Booking id: {} cancelled successfully by admin id: {}", id, user.getId());
    }

    @Override
    @Transactional
    public void cancelMyBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourcesNotFoundException("Booking with id: " + id + " is not found!"));
        User user = userService.getCurrentUser();
        if (!booking.getBooker().getId().equals(user.getId())) {
            log.warn("User id: {} attempted to cancel booking id: {} belonging to another user",
                    user.getId(), id);
            throw new UnauthorizedAccessException("You are not allowed to cancel this booking!");
        }
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessRuleException("Only confirmed bookings can be cancelled!"
            );
        }
        Event event = booking.getEvent();
        if (LocalDateTime.now().plusHours(24).isAfter(event.getStartDateTime())) {
            throw new BusinessRuleException("Bookings cannot be cancelled within 24 hours of the event!");
        }
        performCancelBooking(booking);
        log.info("Booking id: {} cancelled successfully by user id: {}",
                id, user.getId());
    }

    @Override
    public List<BookingResponseDTO> getMyBookings(BookingStatus status) {
        User user = userService.getCurrentUser();
        log.info("Fetching bookings for user id: {}", user.getId());
        List<Booking> bookings;

        if(status!=null ){
            bookings=bookingRepository.findByBooker_IdAndStatus(
                    user.getId(), status
            );
        }else{
            bookings=bookingRepository.findByBooker_Id(
                    user.getId()
            );
        }
        return bookings
                .stream()
                .map(bookingMapper::toResponseDTO)
                .toList();
    }

    @Override
    public List<BookingResponseDTO> getMyEventBookings(Long eventId) {
        User organizer = userService.getCurrentUser();
        log.info("Organizer id: {} requesting bookings for event id: {}",
                organizer.getId(), eventId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourcesNotFoundException("Event with id:" + eventId + " is not found!"));
        if (!event.getOrganizer().getId().equals(organizer.getId())) {
            log.warn("User id: {} attempted to access bookings for event id: {} without being the organizer",
                    organizer.getId(), eventId);
            throw new UnauthorizedAccessException(
                    "You are not allowed to view bookings for this event!"
            );
        }
        return bookingRepository.findBookingsByEventAndOrganizer(
                        eventId,
                        organizer.getId()
                )
                .stream()
                .map(bookingMapper::toResponseDTO)
                .toList();
    }

    private void performCancelBooking(Booking booking) {
        Event event = booking.getEvent();
        event.setAvailableSeats(event.getAvailableSeats() + booking.getSeatsBooked());
        booking.setStatus(BookingStatus.CANCELLED);
        waitlistService.processWaitlist(event);
        eventRepository.save(event);
        bookingRepository.save(booking);
    }
}
