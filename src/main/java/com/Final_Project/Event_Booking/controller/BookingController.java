package com.Final_Project.Event_Booking.controller;

import com.Final_Project.Event_Booking.model.dto.request.BookingRequestDTO;
import com.Final_Project.Event_Booking.model.dto.response.BookingCreationResponseDTO;
import com.Final_Project.Event_Booking.model.dto.response.BookingResponseDTO;
import com.Final_Project.Event_Booking.model.enums.BookingStatus;
import com.Final_Project.Event_Booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bookings")
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    @PreAuthorize("hasRole('ATTENDEE')")
    public ResponseEntity<BookingCreationResponseDTO> createBooking(
            @Valid @RequestBody BookingRequestDTO request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(bookingService.createBooking(request));
    }

    @GetMapping("/{bookingId}")
    @PreAuthorize("hasAnyRole('ATTENDEE', 'ADMIN')")
    public ResponseEntity<BookingResponseDTO> getBooking(
            @PathVariable Long bookingId
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bookingService.getBooking(bookingId));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingResponseDTO>> getAllBookings(){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bookingService.getAllBookings());
    }

    @GetMapping("/my-bookings")
    @PreAuthorize("hasRole('ATTENDEE')")
    public ResponseEntity<List<BookingResponseDTO>> getMyBookings(
            @RequestParam(required = false) BookingStatus status
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bookingService.getMyBookings(status));
    }

    @GetMapping("/my-events-bookings/{eventId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<List<BookingResponseDTO>> getMyEventBookings(
            @PathVariable Long eventId
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bookingService.getMyEventBookings(eventId));
    }

    @DeleteMapping("/{bookingId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> cancelBooking(
            @PathVariable Long bookingId
    ) {
        bookingService.cancelBooking(bookingId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/my-bookings/{bookingId}")
    @PreAuthorize("hasRole('ATTENDEE')")
    public ResponseEntity<Void> cancelMyBooking(
            @PathVariable Long bookingId
    ) {
        bookingService.cancelMyBooking(bookingId);
        return ResponseEntity.noContent().build();
    }
}
