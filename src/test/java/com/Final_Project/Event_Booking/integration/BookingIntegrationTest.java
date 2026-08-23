package com.Final_Project.Event_Booking.integration;

import com.Final_Project.Event_Booking.model.dto.request.BookingRequestDTO;
import com.Final_Project.Event_Booking.model.entity.Booking;
import com.Final_Project.Event_Booking.model.entity.Category;
import com.Final_Project.Event_Booking.model.entity.Event;
import com.Final_Project.Event_Booking.model.entity.User;
import com.Final_Project.Event_Booking.model.entity.Venue;
import com.Final_Project.Event_Booking.model.enums.BookingStatus;
import com.Final_Project.Event_Booking.model.enums.EventStatus;
import com.Final_Project.Event_Booking.model.enums.UserRole;
import com.Final_Project.Event_Booking.repository.BookingRepository;
import com.Final_Project.Event_Booking.repository.CategoryRepository;
import com.Final_Project.Event_Booking.repository.EventRepository;
import com.Final_Project.Event_Booking.repository.UserRepository;
import com.Final_Project.Event_Booking.repository.VenueRepository;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("integrationtest")
@Transactional
class BookingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private User attendee;
    private Event event;

    @BeforeEach
    void setUp() {

        attendee = userRepository.save(
                User.builder()
                        .username("attendee")
                        .email("attendee@email.com")
                        .password("password")
                        .role(UserRole.ATTENDEE)
                        .active(true)
                        .build()
        );

        User organizer = userRepository.save(
                User.builder()
                        .username("organizer")
                        .email("organizer@email.com")
                        .password("password")
                        .role(UserRole.ORGANIZER)
                        .active(true)
                        .build()
        );

        Venue venue = venueRepository.save(
                Venue.builder()
                        .name("Test Venue")
                        .address("Test Address")
                        .city("Test City")
                        .capacity(100)
                        .build()
        );

        Category category = categoryRepository.save(
                Category.builder()
                        .name("Test Category")
                        .build()
        );

        event = eventRepository.save(
                Event.builder()
                        .title("Test Event")
                        .description("Test Description")
                        .startDateTime(LocalDateTime.now().plusDays(5))
                        .endDateTime(LocalDateTime.now().plusDays(5).plusHours(2))
                        .price(BigDecimal.valueOf(20))
                        .totalSeats(10)
                        .availableSeats(10)
                        .status(EventStatus.UPCOMING)
                        .organizer(organizer)
                        .venue(venue)
                        .categories(List.of(category))
                        .build()
        );
    }

    @Test
    void attendeeCanBookEvent() throws Exception {

        BookingRequestDTO request = BookingRequestDTO.builder()
                .eventId(event.getId())
                .seatsBooked(2)
                .build();

        mockMvc.perform(
                        post("/bookings")
                                .with(user("attendee")
                                        .roles("ATTENDEE"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated());

        Event updatedEvent = eventRepository
                .findById(event.getId())
                .orElseThrow();

        assertThat(updatedEvent.getAvailableSeats())
                .isEqualTo(8);

        List<Booking> bookings =
                bookingRepository.findByBooker_Id(attendee.getId());

        assertThat(bookings).hasSize(1);

        assertThat(bookings.get(0).getStatus())
                .isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void bookingNonExistingEventFails() throws Exception {

        BookingRequestDTO request = BookingRequestDTO.builder()
                .eventId(999999L)
                .seatsBooked(1)
                .build();

        mockMvc.perform(
                        post("/bookings")
                                .with(user("attendee")
                                        .roles("ATTENDEE"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound());

        assertThat(
                bookingRepository.findByBooker_Id(attendee.getId())
        ).isEmpty();
    }
}