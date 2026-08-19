package com.Final_Project.Event_Booking.service.impl;

import com.Final_Project.Event_Booking.exception.custom.ResourcesNotFoundException;
import com.Final_Project.Event_Booking.model.dto.request.ReviewRequestDTO;
import com.Final_Project.Event_Booking.model.dto.response.ReviewResponseDTO;
import com.Final_Project.Event_Booking.model.entity.Event;
import com.Final_Project.Event_Booking.model.entity.Review;
import com.Final_Project.Event_Booking.model.entity.User;
import com.Final_Project.Event_Booking.model.enums.BookingStatus;
import com.Final_Project.Event_Booking.model.enums.UserRole;
import com.Final_Project.Event_Booking.model.mapper.ReviewMapper;
import com.Final_Project.Event_Booking.repository.BookingRepository;
import com.Final_Project.Event_Booking.repository.EventRepository;
import com.Final_Project.Event_Booking.repository.ReviewRepository;
import com.Final_Project.Event_Booking.service.ReviewService;
import com.Final_Project.Event_Booking.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;

    private final UserService userService;
    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;

    @Override
    public ReviewResponseDTO createReview(ReviewRequestDTO request) {
        User user=userService.getCurrentUser();
        if(user.getRole()!= UserRole.ATTENDEE){
            throw new IllegalArgumentException("Only attendees can create reviews!");
        }
        Event event=eventRepository.findById(request.getEventId())
                .orElseThrow(()->new ResourcesNotFoundException("Event with id:"+request.getEventId()+" is not found"));
        if(!LocalDateTime.now().isAfter(event.getEndDateTime())){
            throw new IllegalArgumentException("Cant review an event if it has not ended yet.");
        }
        boolean hasBooking=bookingRepository.existsByBooker_IdAndEvent_IdAndStatus(
                user.getId(),
                event.getId(),
                BookingStatus.CONFIRMED
        );
        if(!hasBooking){
            throw new IllegalArgumentException("Cant review an event if you dont have a booking!");
        }
        boolean isReviewed=reviewRepository.existsByReviewer_IdAndEvent_Id(
                user.getId(),
                event.getId()
        );
        if(isReviewed){
            throw new IllegalArgumentException("Cant review an event that you already reviewed before!");
        }
        Review review=reviewMapper.toEntity(request);
        review.setReviewer(user);
        review.setEvent(event);
        review.setCreatedAt(LocalDateTime.now());

        Review savedReview=reviewRepository.save(review);

        return reviewMapper.toResponseDTO(savedReview);
    }

    @Override
    public ReviewResponseDTO getReview(Long id) {
        Review review = reviewRepository.findById(id)
            .orElseThrow(()->new ResourcesNotFoundException("Review with id:"+id+" is not found!"));
        return reviewMapper.toResponseDTO(review);
    }

    @Override
    public List<ReviewResponseDTO> getAllReviews() {
        return reviewRepository.findAll()
                .stream()
                .map(reviewMapper::toResponseDTO)
                .toList();
    }

    @Override
    public ReviewResponseDTO updateReview(Long id, ReviewRequestDTO request) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(()->new ResourcesNotFoundException("Review with id:"+id+" is not found!"));
        User user=userService.getCurrentUser();
        if(!review.getReviewer().getId().equals(user.getId())){
            throw new IllegalArgumentException(("You are not allowed to update this review!"));
        }
        reviewMapper.updateEntity(request,review);
        Review updatedReview=reviewRepository.save(review);
         return reviewMapper.toResponseDTO(updatedReview);
    }

    @Override
    public void deleteReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(()->new ResourcesNotFoundException("Review with id:"+id+" is not found!"));
        User user=userService.getCurrentUser();
        if(!review.getReviewer().getId().equals(user.getId())){
            throw new IllegalArgumentException(("You are not allowed to delete this review!"));
        }
        reviewRepository.delete(review);
    }
}
