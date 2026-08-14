package com.Final_Project.Event_Booking.service;

import com.Final_Project.Event_Booking.model.dto.request.UserRequestDTO;
import com.Final_Project.Event_Booking.model.dto.response.UserResponseDTO;

import java.util.List;

public interface UserService {

    UserResponseDTO createUser(UserRequestDTO request);

    List<UserResponseDTO> getAllUsers();

    UserResponseDTO updateUser(Long id,UserRequestDTO request);

    void deleteUser(Long id);
}
