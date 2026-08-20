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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponseDTO login(LoginRequestDTO request) {

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

        return AuthResponseDTO.builder()
                .username(userDetails.getUsername())
                .token(token)
                .role(userRole)
                .build();
    }

    @Override
    public UserResponseDTO register(RegisterRequestDTO registerRequest) {
        if(userRepository.existsByUsername(registerRequest.getUsername())){
            throw new UsernameAlreadyExistsException("Username already exists!");
        }
        if(userRepository.existsByEmail(registerRequest.getEmail())){
            throw new EmailAlreadyExistsException("Email already exists!");
        }
        User user= userMapper.toEntity(registerRequest);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(UserRole.ATTENDEE);
        user.setActive(true);

        User savedUser=userRepository.save(user);
        return userMapper.toResponseDTO(savedUser);
    }
}
