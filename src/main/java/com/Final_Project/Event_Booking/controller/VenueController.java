package com.Final_Project.Event_Booking.controller;

import com.Final_Project.Event_Booking.model.dto.request.VenueRequestDTO;
import com.Final_Project.Event_Booking.model.dto.response.VenueResponseDTO;
import com.Final_Project.Event_Booking.service.VenueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/venues")
@RequiredArgsConstructor
public class VenueController {
    private final VenueService venueService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VenueResponseDTO> createVenue(
            @Valid @RequestBody VenueRequestDTO request
            ){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(venueService.createVenue(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<VenueResponseDTO>> getAllVenues() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(venueService.getAllVenues());
    }

    @GetMapping("/{venueId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VenueResponseDTO> getVenue(
            @PathVariable Long venueId
    ){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(VenueResponseDTO.builder().build());
    }

    @PutMapping("/{venueId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VenueResponseDTO> updateVenue(
            @PathVariable Long venueId,
            @Valid @RequestBody VenueRequestDTO request
    ){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(venueService.updateVenue(venueId,request));
    }

    @DeleteMapping("/{venueId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteVenue(
            @PathVariable Long venueId
    ) {
        venueService.deleteVenue(venueId);
        return ResponseEntity.noContent().build();
    }
}
