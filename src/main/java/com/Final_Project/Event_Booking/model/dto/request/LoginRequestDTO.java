package com.Final_Project.Event_Booking.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequestDTO {

    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
