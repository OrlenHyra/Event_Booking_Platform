package com.Final_Project.Event_Booking.controller;

import com.Final_Project.Event_Booking.model.dto.request.UserRequestDTO;
import com.Final_Project.Event_Booking.model.dto.response.UserResponseDTO;
import com.Final_Project.Event_Booking.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(
            @Valid
            @RequestBody UserRequestDTO request
            ){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.createUser(request));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long userId,
            @Valid
            @RequestBody UserRequestDTO request){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.updateUser(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.getAllUsers());
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long userId
    ){
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
