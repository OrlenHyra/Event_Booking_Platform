package com.Final_Project.Event_Booking.service;

import com.Final_Project.Event_Booking.exception.custom.BusinessRuleException;
import com.Final_Project.Event_Booking.exception.custom.UnauthorizedAccessException;
import com.Final_Project.Event_Booking.model.dto.response.BookingCreationResponseDTO;
import com.Final_Project.Event_Booking.model.dto.response.WaitlistResponseDTO;
import com.Final_Project.Event_Booking.model.entity.Booking;
import com.Final_Project.Event_Booking.model.entity.Event;
import com.Final_Project.Event_Booking.model.entity.User;
import com.Final_Project.Event_Booking.model.entity.Waitlist;
import com.Final_Project.Event_Booking.model.enums.EventStatus;
import com.Final_Project.Event_Booking.model.enums.UserRole;
import com.Final_Project.Event_Booking.model.enums.WaitlistStatus;
import com.Final_Project.Event_Booking.model.mapper.BookingMapper;
import com.Final_Project.Event_Booking.model.mapper.WaitlistMapper;
import com.Final_Project.Event_Booking.repository.BookingRepository;
import com.Final_Project.Event_Booking.repository.EventRepository;
import com.Final_Project.Event_Booking.repository.WaitlistRepository;
import com.Final_Project.Event_Booking.service.impl.WaitlistServiceImpl;
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
class WaitlistServiceImplTest {

    @Mock
    private WaitlistRepository waitlistRepository;

    @Mock
    private WaitlistMapper waitlistMapper;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private WaitlistServiceImpl waitlistService;

    private User attendee;
    private User organizer;
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
                .username("test-organizer")
                .role(UserRole.ORGANIZER)
                .build();

        event = Event.builder()
                .id(10L)
                .title("test event")
                .status(EventStatus.UPCOMING)
                .availableSeats(0)
                .totalSeats(10)
                .organizer(organizer)
                .startDateTime(LocalDateTime.now().plusDays(5))
                .endDateTime(LocalDateTime.now().plusDays(5).plusHours(3))
                .build();
    }

    @Test
    void createsWaitlistSuccessfully() {
        Waitlist mappedWaitlist = new Waitlist();

        when(waitlistRepository.existsByAttendee_IdAndEvent_IdAndStatus(1L, 10L, WaitlistStatus.WAITING))
                .thenReturn(false);
        when(waitlistRepository.findByAttendee_IdAndEvent_Id(1L, 10L))
                .thenReturn(Optional.empty());
        when(waitlistMapper.toEntity(attendee, event, 3))
                .thenReturn(mappedWaitlist);
        when(waitlistRepository.save(mappedWaitlist))
                .thenReturn(mappedWaitlist);
        when(waitlistMapper.toResponseDTO(mappedWaitlist))
                .thenReturn(WaitlistResponseDTO.builder().id(50L).build());

        BookingCreationResponseDTO response =
                waitlistService.createWaitlist(attendee, event, 3);

        assertThat(response.getWaitlist()).isNotNull();
        assertThat(response.getWaitlist().getMessage())
                .isEqualTo("Not enough seats,you have been added to the waitlist!");
        assertThat(mappedWaitlist.getJoinedAt()).isNotNull();
        assertThat(mappedWaitlist.getStatus()).isEqualTo(WaitlistStatus.WAITING);
        assertThat(mappedWaitlist.getSeatsRequested()).isEqualTo(3);
    }

    @Test
    void cannotJoinWaitlistTwice() {
        when(waitlistRepository.existsByAttendee_IdAndEvent_IdAndStatus(1L, 10L, WaitlistStatus.WAITING))
                .thenReturn(true);

        assertThatThrownBy(() ->
                waitlistService.createWaitlist(attendee, event, 3))
                .isInstanceOf(BusinessRuleException.class);

        verify(waitlistRepository, never()).save(any());
        verify(waitlistRepository, never()).findByAttendee_IdAndEvent_Id(any(), any());
    }

    @Test
    void promotesWaitlistEntryWhenSeatsAreAvailable() {
        event.setAvailableSeats(3);

        Waitlist waitlist = Waitlist.builder()
                .id(50L)
                .attendee(attendee)
                .event(event)
                .seatsRequested(2)
                .status(WaitlistStatus.WAITING)
                .build();

        Booking booking = new Booking();

        when(waitlistRepository.findByEventInOrderByJoinedAtAsc(
                10L, WaitlistStatus.WAITING))
                .thenReturn(List.of(waitlist));

        when(bookingMapper.toBookingFromWaitlist(waitlist, event))
                .thenReturn(booking);

        waitlistService.processWaitlist(event);

        verify(bookingRepository).save(booking);
        verify(waitlistRepository).save(waitlist);

        assertThat(event.getAvailableSeats()).isEqualTo(1);
        assertThat(waitlist.getStatus()).isEqualTo(WaitlistStatus.PROMOTED);
    }

    @Test
    void skipsWaitlistEntryThatNeedsTooManySeats() {
        event.setAvailableSeats(1);

        Waitlist tooBigEntry = Waitlist.builder()
                .id(50L)
                .attendee(attendee)
                .event(event)
                .seatsRequested(5)
                .status(WaitlistStatus.WAITING)
                .build();

        Waitlist fittingEntry = Waitlist.builder()
                .id(51L)
                .attendee(attendee)
                .event(event)
                .seatsRequested(1)
                .status(WaitlistStatus.WAITING)
                .build();

        Booking booking = new Booking();

        when(waitlistRepository.findByEventInOrderByJoinedAtAsc(
                10L, WaitlistStatus.WAITING))
                .thenReturn(List.of(tooBigEntry, fittingEntry));

        when(bookingMapper.toBookingFromWaitlist(fittingEntry, event))
                .thenReturn(booking);

        waitlistService.processWaitlist(event);

        verify(bookingMapper, never())
                .toBookingFromWaitlist(eq(tooBigEntry), any());

        verify(bookingMapper)
                .toBookingFromWaitlist(fittingEntry, event);

        assertThat(event.getAvailableSeats()).isEqualTo(0);
        assertThat(fittingEntry.getStatus()).isEqualTo(WaitlistStatus.PROMOTED);
        assertThat(tooBigEntry.getStatus()).isEqualTo(WaitlistStatus.WAITING);
    }

    @Test
    void getsMyWaitlistsWithStatus() {
        Waitlist waitlist = Waitlist.builder()
                .id(50L)
                .attendee(attendee)
                .event(event)
                .status(WaitlistStatus.WAITING)
                .build();

        WaitlistResponseDTO responseDTO =
                WaitlistResponseDTO.builder()
                        .id(50L)
                        .build();

        when(userService.getCurrentUser()).thenReturn(attendee);
        when(waitlistRepository.findByAttendee_IdAndStatus(
                1L, WaitlistStatus.WAITING))
                .thenReturn(List.of(waitlist));

        when(waitlistMapper.toResponseDTO(waitlist))
                .thenReturn(responseDTO);

        List<WaitlistResponseDTO> responses =
                waitlistService.getMyWaitlists(WaitlistStatus.WAITING);

        assertThat(responses).containsExactly(responseDTO);

        verify(waitlistRepository, never())
                .findByAttendee_Id(any());
    }

    @Test
    void getsAllWaitlists() {
        Waitlist waitlist = Waitlist.builder()
                .id(50L)
                .attendee(attendee)
                .event(event)
                .status(WaitlistStatus.WAITING)
                .build();

        WaitlistResponseDTO responseDTO =
                WaitlistResponseDTO.builder()
                        .id(50L)
                        .build();

        when(waitlistRepository.findAll()).thenReturn(List.of(waitlist));
        when(waitlistMapper.toResponseDTO(waitlist))
                .thenReturn(responseDTO);

        List<WaitlistResponseDTO> responses =
                waitlistService.getAllWaitlists(null);

        assertThat(responses).containsExactly(responseDTO);
    }

    @Test
    void getsEventWaitlistForOrganizer() {
        Waitlist waitlist = Waitlist.builder()
                .id(50L)
                .attendee(attendee)
                .event(event)
                .status(WaitlistStatus.WAITING)
                .build();

        WaitlistResponseDTO responseDTO =
                WaitlistResponseDTO.builder()
                        .id(50L)
                        .build();

        when(userService.getCurrentUser()).thenReturn(organizer);
        when(eventRepository.findById(10L))
                .thenReturn(Optional.of(event));
        when(waitlistRepository.findByEvent_Id(10L))
                .thenReturn(List.of(waitlist));
        when(waitlistMapper.toResponseDTO(waitlist))
                .thenReturn(responseDTO);

        List<WaitlistResponseDTO> responses =
                waitlistService.getEventWaitlist(10L);

        assertThat(responses).containsExactly(responseDTO);
    }

    @Test
    void cannotAccessEventWaitlistAsAnotherOrganizer() {
        User otherOrganizer = User.builder()
                .id(77L)
                .role(UserRole.ORGANIZER)
                .build();

        when(userService.getCurrentUser()).thenReturn(otherOrganizer);
        when(eventRepository.findById(10L))
                .thenReturn(Optional.of(event));

        assertThatThrownBy(() ->
                waitlistService.getEventWaitlist(10L))
                .isInstanceOf(UnauthorizedAccessException.class);
    }
}