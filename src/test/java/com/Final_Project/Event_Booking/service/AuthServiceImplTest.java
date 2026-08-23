package com.Final_Project.Event_Booking.service;

import com.Final_Project.Event_Booking.exception.custom.EmailAlreadyExistsException;
import com.Final_Project.Event_Booking.exception.custom.UsernameAlreadyExistsException;
import com.Final_Project.Event_Booking.model.dto.request.LoginRequestDTO;
import com.Final_Project.Event_Booking.model.dto.request.RegisterRequestDTO;
import com.Final_Project.Event_Booking.model.dto.response.AuthResponseDTO;
import com.Final_Project.Event_Booking.model.dto.response.UserResponseDTO;
import com.Final_Project.Event_Booking.model.entity.User;
import com.Final_Project.Event_Booking.model.enums.UserRole;
import com.Final_Project.Event_Booking.model.mapper.UserMapper;
import com.Final_Project.Event_Booking.repository.UserRepository;
import com.Final_Project.Event_Booking.security.jwt.JwtService;
import com.Final_Project.Event_Booking.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private AuthServiceImpl authService;

    private LoginRequestDTO loginRequest;
    private RegisterRequestDTO registerRequest;

    @BeforeEach
    void setUp() {
        loginRequest = LoginRequestDTO.builder()
                .username("test")
                .password("testpassword")
                .build();

        registerRequest = RegisterRequestDTO.builder()
                .username("test")
                .password("testpassword")
                .email("test@email.com")
                .build();
    }

    @Test
    void loginReturnsTokenForValidCredentials() {
        List<GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_ATTENDEE"));

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getAuthorities()).thenReturn((java.util.Collection) authorities);
        when(userDetails.getUsername()).thenReturn("test");
        when(jwtService.generateToken(userDetails)).thenReturn("test-jwt-token");

        AuthResponseDTO response = authService.login(loginRequest);

        assertThat(response.getUsername()).isEqualTo("test");
        assertThat(response.getToken()).isEqualTo("test-jwt-token");
        assertThat(response.getRole()).isEqualTo(UserRole.ATTENDEE);

        verify(jwtService).generateToken(userDetails);
    }

    @Test
    void loginThrowsExceptionForInvalidCredentials() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class);

        verifyNoInteractions(jwtService);
    }

    @Test
    void registerCreatesNewUser() {
        User mappedUser = new User();
        mappedUser.setUsername("test");
        mappedUser.setEmail("test@email.com");
        mappedUser.setPassword("testpassword");

        User savedUser = User.builder()
                .id(1L)
                .username("test")
                .email("test@email.com")
                .password("encoded-testpassword")
                .role(UserRole.ATTENDEE)
                .active(true)
                .build();

        UserResponseDTO expectedResponse = UserResponseDTO.builder()
                .id(1L)
                .username("test")
                .build();

        when(userRepository.existsByUsername("test")).thenReturn(false);
        when(userRepository.existsByEmail("test@email.com")).thenReturn(false);
        when(userMapper.toEntity(registerRequest)).thenReturn(mappedUser);
        when(passwordEncoder.encode("testpassword")).thenReturn("encoded-testpassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toResponseDTO(savedUser)).thenReturn(expectedResponse);

        UserResponseDTO response = authService.register(registerRequest);

        assertThat(response).isEqualTo(expectedResponse);

        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("testpassword");
    }

    @Test
    void registerThrowsExceptionWhenUsernameExists() {
        when(userRepository.existsByUsername("test")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(UsernameAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void registerThrowsExceptionWhenEmailExists() {
        when(userRepository.existsByUsername("test")).thenReturn(false);
        when(userRepository.existsByEmail("test@email.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }
}
