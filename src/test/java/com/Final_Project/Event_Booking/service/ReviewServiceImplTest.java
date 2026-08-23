package com.Final_Project.Event_Booking.service;

import com.Final_Project.Event_Booking.exception.custom.BusinessRuleException;
import com.Final_Project.Event_Booking.exception.custom.UnauthorizedAccessException;
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
import com.Final_Project.Event_Booking.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @Mock
    private UserService userService;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private User attendee;
    private Event endedEvent;
    private ReviewRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        attendee = User.builder()
                .id(1L)
                .username("test")
                .role(UserRole.ATTENDEE)
                .build();

        endedEvent = Event.builder()
                .id(10L)
                .title("test event")
                .startDateTime(LocalDateTime.now().minusDays(2))
                .endDateTime(LocalDateTime.now().minusDays(1))
                .build();

        requestDTO = ReviewRequestDTO.builder()
                .rating(5)
                .comment("test comment")
                .eventId(10L)
                .build();
    }

    @Test
    void shouldCreateReview() {
        Review mappedReview = new Review();

        Review savedReview = Review.builder()
                .id(200L)
                .rating(5)
                .comment("test comment")
                .reviewer(attendee)
                .event(endedEvent)
                .build();

        ReviewResponseDTO expectedResponse = ReviewResponseDTO.builder()
                .id(200L)
                .rating(5)
                .build();

        when(userService.getCurrentUser()).thenReturn(attendee);
        when(eventRepository.findById(10L)).thenReturn(Optional.of(endedEvent));
        when(bookingRepository.existsByBooker_IdAndEvent_IdAndStatus(
                1L, 10L, BookingStatus.CONFIRMED
        )).thenReturn(true);
        when(reviewRepository.existsByReviewer_IdAndEvent_Id(1L, 10L))
                .thenReturn(false);
        when(reviewMapper.toEntity(requestDTO)).thenReturn(mappedReview);
        when(reviewRepository.save(mappedReview)).thenReturn(savedReview);
        when(reviewMapper.toResponseDTO(savedReview)).thenReturn(expectedResponse);

        ReviewResponseDTO response = reviewService.createReview(requestDTO);

        assertThat(response).isEqualTo(expectedResponse);
        assertThat(mappedReview.getReviewer()).isEqualTo(attendee);
        assertThat(mappedReview.getEvent()).isEqualTo(endedEvent);

        verify(reviewRepository).save(mappedReview);
    }

    @Test
    void shouldNotCreateReviewWithoutBooking() {
        when(userService.getCurrentUser()).thenReturn(attendee);
        when(eventRepository.findById(10L)).thenReturn(Optional.of(endedEvent));
        when(bookingRepository.existsByBooker_IdAndEvent_IdAndStatus(
                1L, 10L, BookingStatus.CONFIRMED
        )).thenReturn(false);

        assertThatThrownBy(() -> reviewService.createReview(requestDTO))
                .isInstanceOf(BusinessRuleException.class);

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void shouldNotCreateReviewTwice() {
        when(userService.getCurrentUser()).thenReturn(attendee);
        when(eventRepository.findById(10L)).thenReturn(Optional.of(endedEvent));
        when(bookingRepository.existsByBooker_IdAndEvent_IdAndStatus(
                1L, 10L, BookingStatus.CONFIRMED
        )).thenReturn(true);
        when(reviewRepository.existsByReviewer_IdAndEvent_Id(1L, 10L))
                .thenReturn(true);

        assertThatThrownBy(() -> reviewService.createReview(requestDTO))
                .isInstanceOf(BusinessRuleException.class);

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void shouldGetReview() {
        Review review = Review.builder()
                .id(200L)
                .rating(4)
                .reviewer(attendee)
                .event(endedEvent)
                .build();

        ReviewResponseDTO expectedResponse = ReviewResponseDTO.builder()
                .id(200L)
                .rating(4)
                .build();

        when(reviewRepository.findById(200L)).thenReturn(Optional.of(review));
        when(reviewMapper.toResponseDTO(review)).thenReturn(expectedResponse);

        ReviewResponseDTO response = reviewService.getReview(200L);

        assertThat(response).isEqualTo(expectedResponse);
    }

    @Test
    void shouldUpdateReview() {
        Review review = Review.builder()
                .id(200L)
                .rating(3)
                .reviewer(attendee)
                .event(endedEvent)
                .build();

        ReviewRequestDTO updateRequest = ReviewRequestDTO.builder()
                .rating(4)
                .comment("updated comment")
                .eventId(10L)
                .build();

        ReviewResponseDTO expectedResponse = ReviewResponseDTO.builder()
                .id(200L)
                .rating(4)
                .build();

        when(reviewRepository.findById(200L)).thenReturn(Optional.of(review));
        when(userService.getCurrentUser()).thenReturn(attendee);
        when(reviewRepository.save(review)).thenReturn(review);
        when(reviewMapper.toResponseDTO(review)).thenReturn(expectedResponse);

        ReviewResponseDTO response =
                reviewService.updateReview(200L, updateRequest);

        assertThat(response).isEqualTo(expectedResponse);
        verify(reviewMapper).updateEntity(updateRequest, review);
        verify(reviewRepository).save(review);
    }

    @Test
    void shouldNotUpdateSomeoneElsesReview() {
        User otherUser = User.builder()
                .id(99L)
                .role(UserRole.ATTENDEE)
                .build();

        Review review = Review.builder()
                .id(200L)
                .reviewer(attendee)
                .event(endedEvent)
                .build();

        when(reviewRepository.findById(200L)).thenReturn(Optional.of(review));
        when(userService.getCurrentUser()).thenReturn(otherUser);

        assertThatThrownBy(() ->
                reviewService.updateReview(200L, requestDTO)
        ).isInstanceOf(UnauthorizedAccessException.class);

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void shouldDeleteReview() {
        Review review = Review.builder()
                .id(200L)
                .rating(3)
                .reviewer(attendee)
                .event(endedEvent)
                .build();

        when(reviewRepository.findById(200L)).thenReturn(Optional.of(review));
        when(userService.getCurrentUser()).thenReturn(attendee);

        reviewService.deleteReview(200L);

        verify(reviewRepository).delete(review);
    }

    @Test
    void shouldNotDeleteSomeoneElsesReview() {
        User otherUser = User.builder()
                .id(99L)
                .role(UserRole.ATTENDEE)
                .build();

        Review review = Review.builder()
                .id(200L)
                .reviewer(attendee)
                .event(endedEvent)
                .build();

        when(reviewRepository.findById(200L)).thenReturn(Optional.of(review));
        when(userService.getCurrentUser()).thenReturn(otherUser);

        assertThatThrownBy(() -> reviewService.deleteReview(200L))
                .isInstanceOf(UnauthorizedAccessException.class);

        verify(reviewRepository, never()).delete(any());
    }

    @Test
    void shouldGetReviewsForEvent() {
        Review review = Review.builder()
                .id(200L)
                .rating(4)
                .reviewer(attendee)
                .event(endedEvent)
                .build();

        ReviewResponseDTO responseDTO = ReviewResponseDTO.builder()
                .id(200L)
                .rating(4)
                .build();

        when(eventRepository.findById(10L)).thenReturn(Optional.of(endedEvent));
        when(reviewRepository.findByEvent_Id(10L))
                .thenReturn(List.of(review));
        when(reviewMapper.toResponseDTO(review)).thenReturn(responseDTO);

        List<ReviewResponseDTO> responses =
                reviewService.getEventReviews(10L);

        assertThat(responses).containsExactly(responseDTO);
    }
}
