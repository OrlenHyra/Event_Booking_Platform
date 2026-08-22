package com.Final_Project.Event_Booking.service.impl;

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
import com.Final_Project.Event_Booking.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponseDTO login(LoginRequestDTO request) {
        log.info("Login attempt for username: {}", request.getUsername());
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getUsername(),
                                request.getPassword()
                        )
                );

        UserDetails userDetails =
                (UserDetails) authentication.getPrincipal();

        String token = jwtService.generateToken(userDetails);

        UserRole userRole = UserRole.valueOf(
                userDetails.getAuthorities()
                        .iterator()
                        .next()
                        .getAuthority()
                        .replace("ROLE_", "")
        );
        log.info("User {} logged in successfully with role {}", userDetails.getUsername(), userRole);

        return AuthResponseDTO.builder()
                .username(userDetails.getUsername())
                .token(token)
                .role(userRole)
                .build();
    }

    @Override
    public UserResponseDTO register(RegisterRequestDTO registerRequest) {
        log.info("Registration attempt for username: {}", registerRequest.getUsername());
        if(userRepository.existsByUsername(registerRequest.getUsername())){
            log.warn("Registration rejected: username {} already exists", registerRequest.getEmail());
            throw new UsernameAlreadyExistsException("Username already exists!");
        }
        if(userRepository.existsByEmail(registerRequest.getEmail())){
            log.warn("Registration rejected: email {} already exists", registerRequest.getUsername());
            throw new EmailAlreadyExistsException("Email already exists!");
        }
        User user= userMapper.toEntity(registerRequest);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(UserRole.ATTENDEE);
        user.setActive(true);

        User savedUser=userRepository.save(user);
        log.info("User registered successfully with id: {} and username: {}", savedUser.getId(), savedUser.getUsername());
        return userMapper.toResponseDTO(savedUser);
    }
}
