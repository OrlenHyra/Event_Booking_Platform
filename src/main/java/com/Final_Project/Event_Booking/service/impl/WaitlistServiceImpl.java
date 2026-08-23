package com.Final_Project.Event_Booking.service.impl;

import com.Final_Project.Event_Booking.exception.custom.BusinessRuleException;
import com.Final_Project.Event_Booking.exception.custom.ResourcesNotFoundException;
import com.Final_Project.Event_Booking.exception.custom.UnauthorizedAccessException;
import com.Final_Project.Event_Booking.model.dto.response.BookingCreationResponseDTO;
import com.Final_Project.Event_Booking.model.dto.response.WaitlistResponseDTO;
import com.Final_Project.Event_Booking.model.entity.Booking;
import com.Final_Project.Event_Booking.model.entity.Event;
import com.Final_Project.Event_Booking.model.entity.User;
import com.Final_Project.Event_Booking.model.entity.Waitlist;
import com.Final_Project.Event_Booking.model.enums.WaitlistStatus;
import com.Final_Project.Event_Booking.model.mapper.BookingMapper;
import com.Final_Project.Event_Booking.model.mapper.WaitlistMapper;
import com.Final_Project.Event_Booking.repository.BookingRepository;
import com.Final_Project.Event_Booking.repository.EventRepository;
import com.Final_Project.Event_Booking.repository.WaitlistRepository;
import com.Final_Project.Event_Booking.service.UserService;
import com.Final_Project.Event_Booking.service.WaitlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WaitlistServiceImpl implements WaitlistService {
    private final WaitlistRepository waitlistRepository;
    private final WaitlistMapper waitlistMapper;
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final EventRepository eventRepository;

    private final UserService userService;

    @Override
    public BookingCreationResponseDTO createWaitlist(User user, Event event, Integer seatsRequested) {
        boolean alreadyWaiting = waitlistRepository.existsByAttendee_IdAndEvent_IdAndStatus(
                user.getId(), event.getId(), WaitlistStatus.WAITING);
        if (alreadyWaiting) {
            log.warn("User {} attempted to join waitlist for event {} but is already on the waitlist",
                    user.getId(), event.getId());
            throw new BusinessRuleException("You are already on the waitlist for this event!");
        }

        Waitlist waitlist = waitlistRepository.findByAttendee_IdAndEvent_Id(user.getId(), event.getId())
                .orElseGet(() -> waitlistMapper.toEntity(user, event, seatsRequested));

        waitlist.setSeatsRequested(seatsRequested);
        waitlist.setStatus(WaitlistStatus.WAITING);
        waitlist.setJoinedAt(LocalDateTime.now());

        Waitlist savedWaitlist = waitlistRepository.save(waitlist);
        log.info("User {} added to waitlist for event {} with {} seats requested",
                user.getId(), event.getId(), seatsRequested);

        WaitlistResponseDTO response = waitlistMapper.toResponseDTO(savedWaitlist);
        response.setMessage("Not enough seats,you have been added to the waitlist!");

        return BookingCreationResponseDTO.builder()
                .waitlist(response)
                .build();
    }

    @Override
    public void processWaitlist(Event event) {
        List<Waitlist> waitlistLists=waitlistRepository.findByEventInOrderByJoinedAtAsc(
                event.getId(),
                WaitlistStatus.WAITING);
        log.info("Processing waitlist for event {}. Available seats: {}, waiting users: {}",
                event.getId(), event.getAvailableSeats(), waitlistLists.size());
        for(Waitlist waitlist : waitlistLists){
            if(event.getAvailableSeats()==0) {
                break;
            }
            if(waitlist.getSeatsRequested()<= event.getAvailableSeats()){
                createBookingFromWaitlist(waitlist,event);
            }
        }
    }

    private void createBookingFromWaitlist(Waitlist waitlist, Event event) {
        Booking booking = bookingMapper.toBookingFromWaitlist(waitlist, event);
        bookingRepository.save(booking);
        event.setAvailableSeats(event.getAvailableSeats() - waitlist.getSeatsRequested());
        waitlist.setStatus(WaitlistStatus.PROMOTED);
        waitlistRepository.save(waitlist);
        log.info("Waitlist entry {} converted to booking for user {} and event {}. Seats booked: {}",
                waitlist.getId(), waitlist.getAttendee().getId(), event.getId(), waitlist.getSeatsRequested());
    }

    private void checkAndExpireIfNeeded(Waitlist waitlist) {
        if (waitlist.getStatus() == WaitlistStatus.WAITING
                && LocalDateTime.now().isAfter(waitlist.getEvent().getEndDateTime())) {
            waitlist.setStatus(WaitlistStatus.EXPIRED);
            waitlistRepository.save(waitlist);
        }
    }

    @Override
    public List<WaitlistResponseDTO> getMyWaitlists(WaitlistStatus status) {
        User user = userService.getCurrentUser();
        log.info("Fetching waitlist entries for user id: {}", user.getId());
        List<Waitlist> waitlists;
        if (status != null) {
            waitlists = waitlistRepository.findByAttendee_IdAndStatus(user.getId(), status);
        } else {
            waitlists = waitlistRepository.findByAttendee_Id(user.getId());
        }
        waitlists.forEach(this::checkAndExpireIfNeeded);
        return waitlists
                .stream()
                .map(waitlistMapper::toResponseDTO)
                .toList();
    }

    @Override
    public List<WaitlistResponseDTO> getAllWaitlists(WaitlistStatus status) {
        log.info("Fetching all waitlist entries");
        List<Waitlist> waitlists;
        if (status != null) {
            waitlists = waitlistRepository.findByStatus(status);
        } else {
            waitlists = waitlistRepository.findAll();
        }
        waitlists.forEach(this::checkAndExpireIfNeeded);
        return waitlists
                .stream()
                .map(waitlistMapper::toResponseDTO)
                .toList();
    }

    @Override
    public List<WaitlistResponseDTO> getEventWaitlist(Long eventId) {
        User organizer = userService.getCurrentUser();
        log.info("Organizer id: {} requesting waitlist for event id: {}", organizer.getId(), eventId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourcesNotFoundException("Event with id:" + eventId + " is not found!"));
        if (!event.getOrganizer().getId().equals(organizer.getId())) {
            log.warn("User id: {} attempted to access waitlist for event id: {} without being the organizer",
                    organizer.getId(), eventId);
            throw new UnauthorizedAccessException("You are not allowed to view the waitlist for this event!");
        }
        List<Waitlist> waitlists = waitlistRepository.findByEvent_Id(eventId);
        waitlists.forEach(this::checkAndExpireIfNeeded);
        return waitlists
                .stream()
                .map(waitlistMapper::toResponseDTO)
                .toList();
    }
}
