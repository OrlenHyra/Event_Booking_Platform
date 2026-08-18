package com.Final_Project.Event_Booking.controller;

import com.Final_Project.Event_Booking.model.dto.request.BookingRequestDTO;
import com.Final_Project.Event_Booking.model.dto.response.BookingResponseDTO;
import com.Final_Project.Event_Booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    @PreAuthorize("hasRole('ATTENDEE')")
    public ResponseEntity<BookingResponseDTO> createBooking(
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

    @PutMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDTO> updateBooking(
            @PathVariable Long bookingId,
            @Valid @RequestBody BookingRequestDTO request
    ){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bookingService.updateBooking(bookingId, request));
    }

    @DeleteMapping("/{bookingId}")
    @PreAuthorize("hasAnyRole('ATTENDEE', 'ADMIN')")
    public ResponseEntity<Void> deleteBooking(
            @PathVariable Long bookingId
    ) {
        bookingService.deleteBooking(bookingId);
        return ResponseEntity.noContent().build();
    }
}
