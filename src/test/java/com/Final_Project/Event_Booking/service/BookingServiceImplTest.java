package com.Final_Project.Event_Booking.service;

import com.Final_Project.Event_Booking.exception.custom.BusinessRuleException;
import com.Final_Project.Event_Booking.exception.custom.UnauthorizedAccessException;
import com.Final_Project.Event_Booking.model.dto.request.BookingRequestDTO;
import com.Final_Project.Event_Booking.model.dto.response.BookingCreationResponseDTO;
import com.Final_Project.Event_Booking.model.dto.response.BookingResponseDTO;
import com.Final_Project.Event_Booking.model.dto.response.WaitlistResponseDTO;
import com.Final_Project.Event_Booking.model.entity.Booking;
import com.Final_Project.Event_Booking.model.entity.Event;
import com.Final_Project.Event_Booking.model.entity.User;
import com.Final_Project.Event_Booking.model.enums.BookingStatus;
import com.Final_Project.Event_Booking.model.enums.EventStatus;
import com.Final_Project.Event_Booking.model.enums.UserRole;
import com.Final_Project.Event_Booking.model.mapper.BookingMapper;
import com.Final_Project.Event_Booking.repository.BookingRepository;
import com.Final_Project.Event_Booking.repository.EventRepository;
import com.Final_Project.Event_Booking.service.impl.BookingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private UserService userService;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private WaitlistService waitlistService;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User attendee;
    private User organizer;
    private User admin;
    private Event event;

    @BeforeEach
    void setUp() {
        attendee = User.builder()
                .id(1L)
                .username("test")
                .role(UserRole.ATTENDEE)
                .build();

        organizer = User.builder()
                .id(2L)
                .username("organizer")
                .role(UserRole.ORGANIZER)
                .build();

        admin = User.builder()
                .id(3L)
                .username("admin")
                .role(UserRole.ADMIN)
                .build();

        event = Event.builder()
                .id(10L)
                .title("Test Event")
                .status(EventStatus.UPCOMING)
                .availableSeats(5)
                .totalSeats(10)
                .organizer(organizer)
                .startDateTime(LocalDateTime.now().plusDays(5))
                .endDateTime(LocalDateTime.now().plusDays(5).plusHours(3))
                .build();
    }

    @Test
    void shouldCreateBooking() {
        BookingRequestDTO request = BookingRequestDTO.builder()
                .eventId(10L)
                .seatsBooked(2)
                .build();

        Booking booking = new Booking();

        when(userService.getCurrentUser()).thenReturn(attendee);
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
        when(bookingMapper.toEntity(request)).thenReturn(booking);

        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> {
                    Booking savedBooking = invocation.getArgument(0);
                    savedBooking.setId(100L);
                    return savedBooking;
                });

        BookingResponseDTO responseDTO = BookingResponseDTO.builder()
                .id(100L)
                .build();

        when(bookingMapper.toResponseDTO(any(Booking.class)))
                .thenReturn(responseDTO);

        BookingCreationResponseDTO response = bookingService.createBooking(request);

        assertThat(response.getBooking()).isNotNull();
        assertThat(event.getAvailableSeats()).isEqualTo(3);

        verify(eventRepository).save(event);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void shouldAddToWaitlistWhenSeatsAreNotEnough() {
        event.setAvailableSeats(1);

        BookingRequestDTO request = BookingRequestDTO.builder()
                .eventId(10L)
                .seatsBooked(3)
                .build();

        BookingCreationResponseDTO waitlistResponse =
                BookingCreationResponseDTO.builder()
                        .waitlist(WaitlistResponseDTO.builder().id(1L).build())
                        .build();

        when(userService.getCurrentUser()).thenReturn(attendee);
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
        when(waitlistService.createWaitlist(attendee, event, 3))
                .thenReturn(waitlistResponse);

        BookingCreationResponseDTO response = bookingService.createBooking(request);

        assertThat(response).isEqualTo(waitlistResponse);

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void shouldNotCreateBookingForDraftEvent() {
        event.setStatus(EventStatus.DRAFT);

        BookingRequestDTO request = BookingRequestDTO.builder()
                .eventId(10L)
                .seatsBooked(1)
                .build();

        when(userService.getCurrentUser()).thenReturn(attendee);
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void shouldGetBookingForOwner() {
        Booking booking = Booking.builder()
                .id(100L)
                .booker(attendee)
                .event(event)
                .status(BookingStatus.CONFIRMED)
                .build();

        BookingResponseDTO responseDTO = BookingResponseDTO.builder()
                .id(100L)
                .build();

        when(bookingRepository.findById(100L))
                .thenReturn(Optional.of(booking));
        when(userService.getCurrentUser()).thenReturn(attendee);
        when(bookingMapper.toResponseDTO(booking)).thenReturn(responseDTO);

        BookingResponseDTO response = bookingService.getBooking(100L);

        assertThat(response).isEqualTo(responseDTO);
    }

    @Test
    void shouldNotGetAnotherUsersBooking() {
        User otherUser = User.builder()
                .id(99L)
                .role(UserRole.ATTENDEE)
                .build();

        Booking booking = Booking.builder()
                .id(100L)
                .booker(attendee)
                .event(event)
                .build();

        when(bookingRepository.findById(100L))
                .thenReturn(Optional.of(booking));
        when(userService.getCurrentUser()).thenReturn(otherUser);

        assertThatThrownBy(() -> bookingService.getBooking(100L))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    void shouldCancelBookingAsAdmin() {
        Booking booking = Booking.builder()
                .id(100L)
                .booker(attendee)
                .event(event)
                .status(BookingStatus.CONFIRMED)
                .seatsBooked(2)
                .build();

        when(bookingRepository.findById(100L))
                .thenReturn(Optional.of(booking));
        when(userService.getCurrentUser()).thenReturn(admin);

        bookingService.cancelBooking(100L);

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(event.getAvailableSeats()).isEqualTo(7);

        verify(waitlistService).processWaitlist(event);
        verify(eventRepository).save(event);
        verify(bookingRepository).save(booking);
    }

    @Test
    void shouldNotCancelBookingAsNonAdmin() {
        Booking booking = Booking.builder()
                .id(100L)
                .booker(attendee)
                .event(event)
                .status(BookingStatus.CONFIRMED)
                .build();

        when(bookingRepository.findById(100L))
                .thenReturn(Optional.of(booking));
        when(userService.getCurrentUser()).thenReturn(attendee);

        assertThatThrownBy(() -> bookingService.cancelBooking(100L))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    void shouldCancelMyBooking() {
        Booking booking = Booking.builder()
                .id(100L)
                .booker(attendee)
                .event(event)
                .status(BookingStatus.CONFIRMED)
                .seatsBooked(2)
                .build();

        when(bookingRepository.findById(100L))
                .thenReturn(Optional.of(booking));
        when(userService.getCurrentUser()).thenReturn(attendee);

        bookingService.cancelMyBooking(100L);

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(event.getAvailableSeats()).isEqualTo(7);

        verify(waitlistService).processWaitlist(event);
    }

    @Test
    void shouldNotCancelBookingWithin24Hours() {
        event.setStartDateTime(LocalDateTime.now().plusHours(5));

        Booking booking = Booking.builder()
                .id(100L)
                .booker(attendee)
                .event(event)
                .status(BookingStatus.CONFIRMED)
                .build();

        when(bookingRepository.findById(100L))
                .thenReturn(Optional.of(booking));
        when(userService.getCurrentUser()).thenReturn(attendee);

        assertThatThrownBy(() -> bookingService.cancelMyBooking(100L))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void shouldGetMyBookingsWithStatus() {
        Booking booking = Booking.builder()
                .id(100L)
                .booker(attendee)
                .event(event)
                .status(BookingStatus.CONFIRMED)
                .build();

        BookingResponseDTO responseDTO = BookingResponseDTO.builder()
                .id(100L)
                .build();

        when(userService.getCurrentUser()).thenReturn(attendee);
        when(bookingRepository.findByBooker_IdAndStatus(
                attendee.getId(),
                BookingStatus.CONFIRMED
        )).thenReturn(List.of(booking));

        when(bookingMapper.toResponseDTO(booking))
                .thenReturn(responseDTO);

        List<BookingResponseDTO> response =
                bookingService.getMyBookings(BookingStatus.CONFIRMED);

        assertThat(response).containsExactly(responseDTO);
    }
}