package com.Final_Project.Event_Booking.integration;

import com.Final_Project.Event_Booking.model.dto.request.LoginRequestDTO;
import com.Final_Project.Event_Booking.model.dto.request.RegisterRequestDTO;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("integrationtest")
@Transactional
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    @Test
    void registerUser() throws Exception {

        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .username("testuser")
                .email("test@email.com")
                .password("password123")
                .build();

        mockMvc.perform(
                        post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@email.com"))
                .andExpect(jsonPath("$.role").value("ATTENDEE"));

        User user = userRepository
                .findByUsername("testuser")
                .orElseThrow();

        assertThat(user.getUsername()).isEqualTo("testuser");
        assertThat(user.getRole()).isEqualTo(UserRole.ATTENDEE);
        assertThat(user.isActive()).isTrue();

        assertThat(
                passwordEncoder.matches(
                        "password123",
                        user.getPassword()
                )
        ).isTrue();
    }

    @Test
    void registerUserWithExistingUsername() throws Exception {

        User existingUser = User.builder()
                .username("testuser")
                .email("existing@email.com")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.ATTENDEE)
                .active(true)
                .build();

        userRepository.save(existingUser);

        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .username("testuser")
                .email("another@email.com")
                .password("password123")
                .build();

        mockMvc.perform(
                        post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Username already exists!"));

        assertThat(userRepository.findAll()).hasSize(1);
    }

    @Test
    void loginUser() throws Exception {

        User user = User.builder()
                .username("testuser")
                .email("test@email.com")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.ATTENDEE)
                .active(true)
                .build();

        userRepository.save(user);

        LoginRequestDTO request = LoginRequestDTO.builder()
                .username("testuser")
                .password("password123")
                .build();

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("ATTENDEE"));
    }

    @Test
    void loginWithWrongPassword() throws Exception {

        User user = User.builder()
                .username("testuser")
                .email("test@email.com")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.ATTENDEE)
                .active(true)
                .build();

        userRepository.save(user);

        LoginRequestDTO request = LoginRequestDTO.builder()
                .username("testuser")
                .password("wrongpassword")
                .build();

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isInternalServerError());
    }
}