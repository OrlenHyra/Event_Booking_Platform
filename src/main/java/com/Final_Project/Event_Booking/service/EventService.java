package com.Final_Project.Event_Booking.service;

import com.Final_Project.Event_Booking.model.dto.request.EventRequestDTO;
import com.Final_Project.Event_Booking.model.dto.response.EventResponseDTO;

import java.util.List;

public interface EventService {
    EventResponseDTO createEvent(EventRequestDTO request);

    EventResponseDTO getEvent(Long id);

    List<EventResponseDTO> getAllEvents();

    EventResponseDTO updateEvent(Long id,EventRequestDTO request);

    void deleteEvent(Long id);
}
