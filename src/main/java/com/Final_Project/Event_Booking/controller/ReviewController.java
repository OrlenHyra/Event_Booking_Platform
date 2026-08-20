package com.Final_Project.Event_Booking.controller;

import com.Final_Project.Event_Booking.model.dto.request.ReviewRequestDTO;
import com.Final_Project.Event_Booking.model.dto.response.ReviewResponseDTO;
import com.Final_Project.Event_Booking.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasRole('ATTENDEE')")
    public ResponseEntity<ReviewResponseDTO> createReview(
            @Valid @RequestBody ReviewRequestDTO request
    ){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reviewService.createReview(request));
    }

    @GetMapping("/{reviewId}")
    @PreAuthorize("hasAnyRole('ATTENDEE', 'ADMIN')")
    public ResponseEntity<ReviewResponseDTO> getReview(
            @PathVariable Long reviewId
    ){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reviewService.getReview(reviewId));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReviewResponseDTO>> getAllReviews(){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reviewService.getAllReviews());
    }

    @PutMapping("/{reviewId}")
    @PreAuthorize("hasRole('ATTENDEE')")
    public ResponseEntity<ReviewResponseDTO> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewRequestDTO request
    ){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reviewService.updateReview(reviewId, request));
    }

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasRole('ATTENDEE')")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId
    ){
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<ReviewResponseDTO>> getEventReviews(
            @PathVariable Long eventId
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reviewService.getEventReviews(eventId));
    }
}
