package com.Final_Project.Event_Booking.controller;

import com.Final_Project.Event_Booking.model.dto.response.WaitlistResponseDTO;
import com.Final_Project.Event_Booking.model.enums.WaitlistStatus;
import com.Final_Project.Event_Booking.service.WaitlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/waitlists")
public class WaitlistController {
    private final WaitlistService waitlistService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<WaitlistResponseDTO>> getAllWaitlists(
            @RequestParam(required = false) WaitlistStatus status
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(waitlistService.getAllWaitlists(status));
    }

    @GetMapping("/my-events-waitlist/{eventId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<List<WaitlistResponseDTO>> getEventWaitlist(
            @PathVariable Long eventId
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(waitlistService.getEventWaitlist(eventId));
    }

    @GetMapping("/my-waitlists")
    @PreAuthorize("hasRole('ATTENDEE')")
    public ResponseEntity<List<WaitlistResponseDTO>> getMyWaitlists(
            @RequestParam(required = false) WaitlistStatus status
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(waitlistService.getMyWaitlists(status));
    }
}