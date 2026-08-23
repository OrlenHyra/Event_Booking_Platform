package com.Final_Project.Event_Booking.integration;

import com.Final_Project.Event_Booking.model.dto.request.CategoryRequestDTO;
import com.Final_Project.Event_Booking.model.dto.request.EventRequestDTO;
import com.Final_Project.Event_Booking.model.entity.User;
import com.Final_Project.Event_Booking.model.enums.UserRole;
import com.Final_Project.Event_Booking.repository.UserRepository;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("integrationtest")
@Transactional
class SecurityAccessIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @BeforeEach
    void setUp() {

        userRepository.deleteAll();

        userRepository.save(User.builder()
                .username("test-admin")
                .email("test-admin@email.com")
                .password(passwordEncoder.encode("testpassword"))
                .role(UserRole.ADMIN)
                .active(true)
                .build());

        userRepository.save(User.builder()
                .username("test-attendee")
                .email("test-attendee@email.com")
                .password(passwordEncoder.encode("testpassword"))
                .role(UserRole.ATTENDEE)
                .active(true)
                .build());

        userRepository.save(User.builder()
                .username("test-organizer")
                .email("test-organizer@email.com")
                .password(passwordEncoder.encode("testpassword"))
                .role(UserRole.ORGANIZER)
                .active(true)
                .build());
    }


    @Test
    void getAllEvents_withoutAuthentication_isPubliclyAccessible()
            throws Exception {

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk());
    }


    @Test
    void createCategory_withoutAuthentication_isRejected()
            throws Exception {

        CategoryRequestDTO request = CategoryRequestDTO.builder()
                .name("test category")
                .build();

        var result = mockMvc.perform(
                        post("/categories")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andReturn();

        assertThat(result.getResponse().getStatus())
                .isIn(401, 403);
    }


    @Test
    void createCategory_asAttendee_isForbidden()
            throws Exception {

        CategoryRequestDTO request = CategoryRequestDTO.builder()
                .name("test category")
                .build();

        mockMvc.perform(
                        post("/categories")
                                .with(user("test-attendee")
                                        .roles("ATTENDEE"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isForbidden());
    }


    @Test
    void createCategory_asAdmin_isAllowed()
            throws Exception {

        CategoryRequestDTO request = CategoryRequestDTO.builder()
                .name("test category")
                .build();

        mockMvc.perform(
                        post("/categories")
                                .with(user("test-admin")
                                        .roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.name")
                                .value("test category")
                );
    }


    @Test
    void getAllBookings_asOrganizer_isForbidden()
            throws Exception {

        mockMvc.perform(
                        get("/bookings")
                                .with(user("test-organizer")
                                        .roles("ORGANIZER"))
                )
                .andExpect(status().isForbidden());
    }


    @Test
    void createEvent_asAttendee_isForbidden()
            throws Exception {

        EventRequestDTO request = EventRequestDTO.builder()
                .title("test event")
                .description("test description")
                .startDateTime(
                        LocalDateTime.now().plusDays(5)
                )
                .endDateTime(
                        LocalDateTime.now()
                                .plusDays(5)
                                .plusHours(2)
                )
                .price(BigDecimal.TEN)
                .totalSeats(10)
                .venueId(1L)
                .categoryIds(List.of(1L))
                .build();

        mockMvc.perform(
                        post("/events")
                                .with(user("test-attendee")
                                        .roles("ATTENDEE"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isForbidden());
    }


    @Test
    void deleteCategory_asOrganizer_isForbidden()
            throws Exception {

        mockMvc.perform(
                        delete("/categories/1")
                                .with(user("test-organizer")
                                        .roles("ORGANIZER"))
                )
                .andExpect(status().isForbidden());
    }
}