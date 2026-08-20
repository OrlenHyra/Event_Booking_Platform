package com.Final_Project.Event_Booking.service;

import com.Final_Project.Event_Booking.model.dto.request.ReviewRequestDTO;
import com.Final_Project.Event_Booking.model.dto.response.ReviewResponseDTO;

import java.util.List;

public interface ReviewService {
    ReviewResponseDTO createReview(ReviewRequestDTO request);

    ReviewResponseDTO getReview(Long id);

    List<ReviewResponseDTO> getAllReviews();

    ReviewResponseDTO updateReview(Long id, ReviewRequestDTO request);

    void deleteReview(Long id);

    List<ReviewResponseDTO> getEventReviews(Long eventId);
}
