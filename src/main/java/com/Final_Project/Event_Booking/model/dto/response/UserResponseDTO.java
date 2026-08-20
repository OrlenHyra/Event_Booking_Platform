package com.Final_Project.Event_Booking.model.dto.response;

import com.Final_Project.Event_Booking.model.enums.UserRole;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {
    private Long id;

    private String username;

    private String email;

    private UserRole role;

    private boolean active;
}
