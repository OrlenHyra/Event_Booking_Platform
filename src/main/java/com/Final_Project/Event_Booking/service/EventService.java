package com.Final_Project.Event_Booking.service;

import com.Final_Project.Event_Booking.model.dto.request.EventRequestDTO;
import com.Final_Project.Event_Booking.model.dto.response.EventResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    EventResponseDTO createEvent(EventRequestDTO request);

    EventResponseDTO publishEvent(Long id);

    List<EventResponseDTO> getOrganizerEvents();

    List<EventResponseDTO> getAllEventsForAdmin();

    EventResponseDTO getEvent(Long id);

    List<EventResponseDTO> getAllEvents();

    EventResponseDTO updateEvent(Long id,EventRequestDTO request);

    EventResponseDTO cancelEvent(Long id);

    Page<EventResponseDTO> getEventsByCategory(String categoryName, Pageable pageable);

    Page<EventResponseDTO> getEventsByCity(String cityName,Pageable pageable);

    Page<EventResponseDTO> filterByDateRange(LocalDateTime startDateTime,LocalDateTime endDateTime,Pageable pageable);

    Page<EventResponseDTO> filterByPriceRange(BigDecimal minPrice, BigDecimal maxPrice,Pageable pageable);
}
