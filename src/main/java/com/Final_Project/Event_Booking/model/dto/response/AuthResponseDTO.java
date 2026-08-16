package com.Final_Project.Event_Booking.model.dto.response;

import com.Final_Project.Event_Booking.model.enums.UserRole;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDTO {

    private String username;

    private String token;

    private UserRole role;
}
