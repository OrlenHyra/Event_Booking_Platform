package com.Final_Project.Event_Booking.service;

import com.Final_Project.Event_Booking.model.dto.request.UserRequestDTO;
import com.Final_Project.Event_Booking.model.dto.response.UserResponseDTO;
import com.Final_Project.Event_Booking.model.entity.Event;
import com.Final_Project.Event_Booking.model.entity.User;
import com.Final_Project.Event_Booking.model.enums.UserRole;

import java.util.List;

public interface UserService {

    UserResponseDTO createUser(UserRequestDTO request);

    List<UserResponseDTO> getAllUsers();

    UserResponseDTO updateUser(Long id,UserRequestDTO request);

    void deleteUser(Long id);

    UserResponseDTO updateRole(Long id, UserRole role);

    User getCurrentUser();

    boolean isCurrentUserOwner(Event event);
}
