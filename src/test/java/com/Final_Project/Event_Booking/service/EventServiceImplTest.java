package com.Final_Project.Event_Booking.service;

import com.Final_Project.Event_Booking.exception.custom.BusinessRuleException;
import com.Final_Project.Event_Booking.exception.custom.UnauthorizedAccessException;
import com.Final_Project.Event_Booking.model.dto.request.EventRequestDTO;
import com.Final_Project.Event_Booking.model.dto.response.EventResponseDTO;
import com.Final_Project.Event_Booking.model.entity.Category;
import com.Final_Project.Event_Booking.model.entity.Event;
import com.Final_Project.Event_Booking.model.entity.Review;
import com.Final_Project.Event_Booking.model.entity.User;
import com.Final_Project.Event_Booking.model.entity.Venue;
import com.Final_Project.Event_Booking.model.enums.EventStatus;
import com.Final_Project.Event_Booking.model.enums.UserRole;
import com.Final_Project.Event_Booking.model.mapper.EventMapper;
import com.Final_Project.Event_Booking.repository.CategoryRepository;
import com.Final_Project.Event_Booking.repository.EventRepository;
import com.Final_Project.Event_Booking.repository.VenueRepository;
import com.Final_Project.Event_Booking.service.impl.EventServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventMapper eventMapper;

    @Mock
    private VenueRepository venueRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private EventServiceImpl eventService;

    private User organizer;
    private User admin;
    private Venue venue;
    private Category category;
    private EventRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        organizer = User.builder()
                .id(2L)
                .username("test-organizer")
                .role(UserRole.ORGANIZER)
                .build();

        admin = User.builder()
                .id(3L)
                .username("test-admin")
                .role(UserRole.ADMIN)
                .build();

        venue = Venue.builder()
                .id(5L)
                .name("test venue")
                .address("test address")
                .city("test city")
                .capacity(100)
                .build();

        category = Category.builder()
                .id(7L)
                .name("test category")
                .build();

        requestDTO = EventRequestDTO.builder()
                .title("test event")
                .description("test description")
                .startDateTime(LocalDateTime.now().plusDays(10))
                .endDateTime(LocalDateTime.now().plusDays(10).plusHours(3))
                .price(BigDecimal.valueOf(20))
                .totalSeats(50)
                .venueId(5L)
                .categoryIds(List.of(7L))
                .build();
    }

    private Event createDraftEvent() {
        return Event.builder()
                .id(1L)
                .title("test event")
                .status(EventStatus.DRAFT)
                .organizer(organizer)
                .venue(venue)
                .totalSeats(50)
                .availableSeats(50)
                .startDateTime(LocalDateTime.now().plusDays(10))
                .endDateTime(LocalDateTime.now().plusDays(10).plusHours(3))
                .build();
    }

    @Test
    void shouldCreateEvent() {
        Event mappedEvent = new Event();
        Event savedEvent = createDraftEvent();

        EventResponseDTO expectedResponse = EventResponseDTO.builder()
                .id(1L)
                .title("test event")
                .build();

        when(userService.getCurrentUser()).thenReturn(organizer);
        when(venueRepository.findById(5L)).thenReturn(Optional.of(venue));
        when(eventRepository.findOverlappingEventId(eq(5L), any(), any()))
                .thenReturn(null);
        when(categoryRepository.findAllById(List.of(7L)))
                .thenReturn(List.of(category));
        when(eventMapper.toEntity(requestDTO)).thenReturn(mappedEvent);
        when(eventRepository.save(mappedEvent)).thenReturn(savedEvent);
        when(eventMapper.toResponseDTO(savedEvent)).thenReturn(expectedResponse);

        EventResponseDTO response = eventService.createEvent(requestDTO);

        assertThat(response).isEqualTo(expectedResponse);
        assertThat(mappedEvent.getOrganizer()).isEqualTo(organizer);
        assertThat(mappedEvent.getVenue()).isEqualTo(venue);
        assertThat(mappedEvent.getAvailableSeats()).isEqualTo(50);
        assertThat(mappedEvent.getStatus()).isEqualTo(EventStatus.DRAFT);

        verify(eventRepository).save(mappedEvent);
    }

    @Test
    void shouldNotCreateEventWithInvalidDates() {
        EventRequestDTO invalidRequest = EventRequestDTO.builder()
                .title("test event")
                .description("test description")
                .startDateTime(LocalDateTime.now().plusDays(10))
                .endDateTime(LocalDateTime.now().plusDays(9))
                .price(BigDecimal.TEN)
                .totalSeats(10)
                .venueId(5L)
                .categoryIds(List.of(7L))
                .build();

        when(userService.getCurrentUser()).thenReturn(organizer);

        assertThatThrownBy(() -> eventService.createEvent(invalidRequest))
                .isInstanceOf(BusinessRuleException.class);

        verifyNoInteractions(venueRepository, categoryRepository);
    }

    @Test
    void shouldPublishEvent() {
        Event event = createDraftEvent();

        EventResponseDTO expectedResponse = EventResponseDTO.builder()
                .id(1L)
                .status(EventStatus.UPCOMING)
                .build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userService.getCurrentUser()).thenReturn(organizer);
        when(userService.isCurrentUserOwner(event)).thenReturn(true);
        when(eventRepository.save(event)).thenReturn(event);
        when(eventMapper.toResponseDTO(event)).thenReturn(expectedResponse);

        EventResponseDTO response = eventService.publishEvent(1L);

        assertThat(response).isEqualTo(expectedResponse);
        assertThat(event.getStatus()).isEqualTo(EventStatus.UPCOMING);

        verify(eventRepository).save(event);
    }

    @Test
    void shouldNotPublishEventWhenUserIsNotOwner() {
        Event event = createDraftEvent();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userService.getCurrentUser()).thenReturn(organizer);
        when(userService.isCurrentUserOwner(event)).thenReturn(false);

        assertThatThrownBy(() -> eventService.publishEvent(1L))
                .isInstanceOf(UnauthorizedAccessException.class);

        verify(eventRepository, never()).save(any());
    }

    @Test
    void shouldReturnEventWithAverageRating() {
        Event event = createDraftEvent();
        event.setStatus(EventStatus.COMPLETED);
        event.setReviews(List.of(
                Review.builder().rating(4).build(),
                Review.builder().rating(2).build()
        ));

        EventResponseDTO responseDTO = EventResponseDTO.builder()
                .id(1L)
                .build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventMapper.toResponseDTO(event)).thenReturn(responseDTO);

        EventResponseDTO response = eventService.getEvent(1L);

        assertThat(response.getAverageRating()).isEqualTo(3.0);
    }

    @Test
    void shouldUpdateEvent() {
        Event event = createDraftEvent();

        EventResponseDTO expectedResponse = EventResponseDTO.builder()
                .id(1L)
                .title("updated test event")
                .build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userService.getCurrentUser()).thenReturn(organizer);
        when(userService.isCurrentUserOwner(event)).thenReturn(true);
        when(venueRepository.findById(5L)).thenReturn(Optional.of(venue));
        when(eventRepository.findOverlappingEventIdForUpdate(
                eq(1L), eq(5L), any(), any()
        )).thenReturn(null);
        when(categoryRepository.findAllById(List.of(7L)))
                .thenReturn(List.of(category));
        when(eventRepository.save(event)).thenReturn(event);
        when(eventMapper.toResponseDTO(event)).thenReturn(expectedResponse);

        EventResponseDTO response = eventService.updateEvent(1L, requestDTO);

        assertThat(response).isEqualTo(expectedResponse);
        verify(eventMapper).updateEntity(requestDTO, event);
        verify(eventRepository).save(event);
    }

    @Test
    void shouldNotUpdateEventWhenUserIsNotOwner() {
        Event event = createDraftEvent();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userService.getCurrentUser()).thenReturn(organizer);
        when(userService.isCurrentUserOwner(event)).thenReturn(false);

        assertThatThrownBy(() -> eventService.updateEvent(1L, requestDTO))
                .isInstanceOf(UnauthorizedAccessException.class);

        verify(eventRepository, never()).save(any());
    }

    @Test
    void shouldCancelEvent() {
        Event event = createDraftEvent();
        event.setStatus(EventStatus.UPCOMING);

        EventResponseDTO expectedResponse = EventResponseDTO.builder()
                .id(1L)
                .status(EventStatus.CANCELLED)
                .build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userService.getCurrentUser()).thenReturn(organizer);
        when(userService.isCurrentUserOwner(event)).thenReturn(true);
        when(eventRepository.save(event)).thenReturn(event);
        when(eventMapper.toResponseDTO(event)).thenReturn(expectedResponse);

        EventResponseDTO response = eventService.cancelEvent(1L);

        assertThat(response).isEqualTo(expectedResponse);
        assertThat(event.getStatus()).isEqualTo(EventStatus.CANCELLED);

        verify(eventRepository).save(event);
    }

    @Test
    void shouldNotCancelAlreadyCancelledEvent() {
        Event event = createDraftEvent();
        event.setStatus(EventStatus.CANCELLED);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userService.getCurrentUser()).thenReturn(organizer);
        when(userService.isCurrentUserOwner(event)).thenReturn(true);

        assertThatThrownBy(() -> eventService.cancelEvent(1L))
                .isInstanceOf(BusinessRuleException.class);

        verify(eventRepository, never()).save(any());
    }

    @Test
    void shouldReturnEventsByCategory() {
        Event event = createDraftEvent();
        event.setStatus(EventStatus.UPCOMING);

        EventResponseDTO responseDTO = EventResponseDTO.builder()
                .id(1L)
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Event> page = new PageImpl<>(List.of(event), pageable, 1);

        when(eventRepository.findByStatusInAndCategories_Name(
                List.of(
                        EventStatus.ACTIVE,
                        EventStatus.COMPLETED,
                        EventStatus.UPCOMING
                ),
                "test category",
                pageable
        )).thenReturn(page);

        when(eventMapper.toResponseDTO(event)).thenReturn(responseDTO);

        Page<EventResponseDTO> responsePage =
                eventService.getEventsByCategory("test category", pageable);

        assertThat(responsePage.getContent())
                .containsExactly(responseDTO);
    }

    @Test
    void shouldFilterEventsByPrice() {
        Event event = createDraftEvent();

        EventResponseDTO responseDTO = EventResponseDTO.builder()
                .id(1L)
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Event> page = new PageImpl<>(List.of(event), pageable, 1);

        BigDecimal minPrice = BigDecimal.valueOf(10);
        BigDecimal maxPrice = BigDecimal.valueOf(100);

        when(eventRepository.findEventByPriceRange(
                minPrice, maxPrice, pageable
        )).thenReturn(page);

        when(eventMapper.toResponseDTO(event)).thenReturn(responseDTO);

        Page<EventResponseDTO> responsePage =
                eventService.filterByPriceRange(
                        minPrice,
                        maxPrice,
                        pageable
                );

        assertThat(responsePage.getContent())
                .containsExactly(responseDTO);
    }
}