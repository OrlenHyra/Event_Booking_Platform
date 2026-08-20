package com.Final_Project.Event_Booking.controller;

import com.Final_Project.Event_Booking.model.dto.request.EventRequestDTO;
import com.Final_Project.Event_Booking.model.dto.response.EventResponseDTO;
import com.Final_Project.Event_Booking.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/events")
public class EventController {
    private final EventService eventService;

    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<EventResponseDTO> createEvent(
            @Valid @RequestBody EventRequestDTO request
            ){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(eventService.createEvent(request));
    }

    @PutMapping("/{eventId}/publish")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<EventResponseDTO> publishEvent(
            @PathVariable Long eventId
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(eventService.publishEvent(eventId));
    }

    @GetMapping("/my-events")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<List<EventResponseDTO>> getOrganizerEvents() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(eventService.getOrganizerEvents());
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EventResponseDTO>> getAllEventsForAdmin() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(eventService.getAllEventsForAdmin());
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponseDTO> getEvent(
            @PathVariable Long eventId
    ){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(eventService.getEvent(eventId));
    }

    @GetMapping
    public ResponseEntity<List<EventResponseDTO>> getAllEvents(){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(eventService.getAllEvents());
    }

    @PutMapping("/{eventId}")
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    public ResponseEntity<EventResponseDTO> updateEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody EventRequestDTO request
    ){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(eventService.updateEvent(eventId, request));
    }

    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<EventResponseDTO> cancelEvent(
            @PathVariable Long eventId
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(eventService.cancelEvent(eventId));
    }

    @GetMapping("/category/{categoryName}")
    public ResponseEntity<Page<EventResponseDTO>> getEventsByCategory(
            @PathVariable String categoryName,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(eventService.getEventsByCategory(categoryName, pageable));
    }

    @GetMapping("/city/{cityName}")
    public ResponseEntity<Page<EventResponseDTO>> getEventsByCity(
            @PathVariable String cityName,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(eventService.getEventsByCity(cityName,pageable));
    }

    @GetMapping("/date-range")
    public ResponseEntity<Page<EventResponseDTO>> getEventsByDateRange(
            @RequestParam LocalDateTime startDateTime,
            @RequestParam LocalDateTime endDateTime,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(eventService.filterByDateRange(startDateTime, endDateTime, pageable));
    }

    @GetMapping("/price-range")
    public ResponseEntity<Page<EventResponseDTO>> getEventsByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(eventService.filterByPriceRange(minPrice, maxPrice,pageable));
    }
}
