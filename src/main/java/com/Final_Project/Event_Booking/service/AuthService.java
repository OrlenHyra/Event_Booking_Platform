package com.Final_Project.Event_Booking.service;

import com.Final_Project.Event_Booking.model.dto.request.LoginRequestDTO;
import com.Final_Project.Event_Booking.model.dto.request.RegisterRequestDTO;
import com.Final_Project.Event_Booking.model.dto.response.AuthResponseDTO;
import com.Final_Project.Event_Booking.model.dto.response.UserResponseDTO;

public interface AuthService {
    AuthResponseDTO login(LoginRequestDTO request);

    UserResponseDTO register(RegisterRequestDTO registerRequest);
}
