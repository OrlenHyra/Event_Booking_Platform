package com.Final_Project.Event_Booking.integration;

import com.Final_Project.Event_Booking.model.dto.request.EventRequestDTO;
import com.Final_Project.Event_Booking.model.entity.Category;
import com.Final_Project.Event_Booking.model.entity.Event;
import com.Final_Project.Event_Booking.model.entity.User;
import com.Final_Project.Event_Booking.model.entity.Venue;
import com.Final_Project.Event_Booking.model.enums.EventStatus;
import com.Final_Project.Event_Booking.model.enums.UserRole;
import com.Final_Project.Event_Booking.repository.CategoryRepository;
import com.Final_Project.Event_Booking.repository.EventRepository;
import com.Final_Project.Event_Booking.repository.UserRepository;
import com.Final_Project.Event_Booking.repository.VenueRepository;
import tools.jackson.databind.JsonNode;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("integrationtest")
@Transactional
class EventIntegrationTest {

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

    private User organizer;
    private Venue venue;
    private Category category;

    @BeforeEach
    void setUp() {

        organizer = userRepository.save(
                User.builder()
                        .username("organizer")
                        .email("organizer@email.com")
                        .password("password")
                        .role(UserRole.ORGANIZER)
                        .active(true)
                        .build()
        );

        venue = venueRepository.save(
                Venue.builder()
                        .name("Test Venue")
                        .address("Test Address")
                        .city("Test City")
                        .capacity(100)
                        .build()
        );

        category = categoryRepository.save(
                Category.builder()
                        .name("Test Category")
                        .build()
        );
    }

    private EventRequestDTO buildEventRequest() {

        return EventRequestDTO.builder()
                .title("Test Event")
                .description("Test Description")
                .startDateTime(LocalDateTime.now().plusDays(5))
                .endDateTime(LocalDateTime.now().plusDays(5).plusHours(2))
                .price(BigDecimal.valueOf(20))
                .totalSeats(50)
                .venueId(venue.getId())
                .categoryIds(List.of(category.getId()))
                .build();
    }

    @Test
    void organizerCanCreateEvent() throws Exception {

        EventRequestDTO request = buildEventRequest();

        String response = mockMvc.perform(
                        post("/events")
                                .with(user("organizer")
                                        .roles("ORGANIZER"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Event"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        Long eventId = json.get("id").asLong();

        Event event = eventRepository
                .findById(eventId)
                .orElseThrow();

        assertThat(event.getTitle()).isEqualTo("Test Event");
        assertThat(event.getTotalSeats()).isEqualTo(50);
        assertThat(event.getOrganizer().getUsername())
                .isEqualTo("organizer");
    }

    @Test
    void attendeeCannotCreateEvent() throws Exception {

        userRepository.save(
                User.builder()
                        .username("attendee")
                        .email("attendee@email.com")
                        .password("password")
                        .role(UserRole.ATTENDEE)
                        .active(true)
                        .build()
        );

        EventRequestDTO request = buildEventRequest();

        mockMvc.perform(
                        post("/events")
                                .with(user("attendee")
                                        .roles("ATTENDEE"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isForbidden());

        assertThat(eventRepository.findAll()).isEmpty();
    }

    @Test
    void organizerCanPublishEvent() throws Exception {

        Event event = eventRepository.save(
                Event.builder()
                        .title("Test Event")
                        .description("Test Description")
                        .startDateTime(LocalDateTime.now().plusDays(5))
                        .endDateTime(LocalDateTime.now().plusDays(5).plusHours(2))
                        .price(BigDecimal.valueOf(20))
                        .totalSeats(50)
                        .availableSeats(50)
                        .status(EventStatus.DRAFT)
                        .organizer(organizer)
                        .venue(venue)
                        .categories(List.of(category))
                        .build()
        );

        mockMvc.perform(
                        put("/events/" + event.getId() + "/publish")
                                .with(user("organizer")
                                        .roles("ORGANIZER"))
                )
                .andExpect(status().isOk());

        Event updatedEvent = eventRepository
                .findById(event.getId())
                .orElseThrow();

        assertThat(updatedEvent.getStatus())
                .isEqualTo(EventStatus.UPCOMING);
    }

    @Test
    void organizerCannotPublishAlreadyPublishedEvent() throws Exception {

        Event event = eventRepository.save(
                Event.builder()
                        .title("Test Event")
                        .description("Test Description")
                        .startDateTime(LocalDateTime.now().plusDays(5))
                        .endDateTime(LocalDateTime.now().plusDays(5).plusHours(2))
                        .price(BigDecimal.valueOf(20))
                        .totalSeats(50)
                        .availableSeats(50)
                        .status(EventStatus.UPCOMING)
                        .organizer(organizer)
                        .venue(venue)
                        .categories(List.of(category))
                        .build()
        );

        mockMvc.perform(
                        put("/events/" + event.getId() + "/publish")
                                .with(user("organizer")
                                        .roles("ORGANIZER"))
                )
                .andExpect(status().isBadRequest());

        Event unchangedEvent = eventRepository
                .findById(event.getId())
                .orElseThrow();

        assertThat(unchangedEvent.getStatus())
                .isEqualTo(EventStatus.UPCOMING);
    }
}