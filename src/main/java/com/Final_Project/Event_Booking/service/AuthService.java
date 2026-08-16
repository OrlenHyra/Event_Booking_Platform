package com.Final_Project.Event_Booking.service;

import com.Final_Project.Event_Booking.model.dto.request.LoginRequestDTO;
import com.Final_Project.Event_Booking.model.dto.response.AuthResponseDTO;

public interface AuthService {
    AuthResponseDTO login(LoginRequestDTO request);
}
