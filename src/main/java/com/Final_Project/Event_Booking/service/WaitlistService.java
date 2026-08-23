package com.Final_Project.Event_Booking.service;

import com.Final_Project.Event_Booking.model.dto.response.BookingCreationResponseDTO;
import com.Final_Project.Event_Booking.model.dto.response.WaitlistResponseDTO;
import com.Final_Project.Event_Booking.model.entity.Event;
import com.Final_Project.Event_Booking.model.entity.User;
import com.Final_Project.Event_Booking.model.entity.Waitlist;
import com.Final_Project.Event_Booking.model.enums.WaitlistStatus;

import java.util.List;

public interface WaitlistService {
    BookingCreationResponseDTO createWaitlist(User user, Event event, Integer seatsRequested);

    void processWaitlist(Event event);

    List<WaitlistResponseDTO> getMyWaitlists(WaitlistStatus status);

    List<WaitlistResponseDTO> getAllWaitlists(WaitlistStatus status);

    List<WaitlistResponseDTO> getEventWaitlist(Long eventId);
}
