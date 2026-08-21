package com.Final_Project.Event_Booking.service;

import com.Final_Project.Event_Booking.model.dto.response.BookingCreationResponseDTO;
import com.Final_Project.Event_Booking.model.entity.Event;
import com.Final_Project.Event_Booking.model.entity.User;

public interface WaitlistService {
    BookingCreationResponseDTO createWaitlist(User user, Event event, Integer seatsRequested);

    void processWaitlist(Event event);
}
