package com.Final_Project.Event_Booking.service.impl;

import com.Final_Project.Event_Booking.exception.custom.BusinessRuleException;
import com.Final_Project.Event_Booking.model.dto.response.BookingCreationResponseDTO;
import com.Final_Project.Event_Booking.model.dto.response.WaitlistResponseDTO;
import com.Final_Project.Event_Booking.model.entity.Booking;
import com.Final_Project.Event_Booking.model.entity.Event;
import com.Final_Project.Event_Booking.model.entity.User;
import com.Final_Project.Event_Booking.model.entity.Waitlist;
import com.Final_Project.Event_Booking.model.mapper.BookingMapper;
import com.Final_Project.Event_Booking.model.mapper.WaitlistMapper;
import com.Final_Project.Event_Booking.repository.BookingRepository;
import com.Final_Project.Event_Booking.repository.WaitlistRepository;
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

    @Override
    public BookingCreationResponseDTO createWaitlist(User user, Event event, Integer seatsRequested) {
        log.warn("User {} attempted to join waitlist for event {} but is already on the waitlist",
                user.getId(), event.getId());
        if(waitlistRepository.existsByAttendee_IdAndEvent_Id(
                user.getId(),
                event.getId()
        )){
            throw new BusinessRuleException("You are already on the waitlist for this event!");
        }
        Waitlist waitlist=waitlistMapper.toEntity(user, event, seatsRequested);
        waitlist.setJoinedAt(LocalDateTime.now());
        Waitlist savedWaitlist=waitlistRepository.save(waitlist);
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
        List<Waitlist> waitlistLists=waitlistRepository.findByEventInOrderByJoinedAtAsc(event.getId());
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

    private void createBookingFromWaitlist(Waitlist waitlist,Event event){
        Booking booking=bookingMapper.toBookingFromWaitlist(waitlist,event);
        bookingRepository.save(booking);
        event.setAvailableSeats(event.getAvailableSeats()-waitlist.getSeatsRequested());
        waitlistRepository.delete(waitlist);
        log.info("Waitlist entry {} converted to booking for user {} and event {}. Seats booked: {}",
                waitlist.getId(), waitlist.getAttendee().getId(), event.getId(), waitlist.getSeatsRequested());
    }
}
