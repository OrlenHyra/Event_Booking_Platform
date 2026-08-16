package com.Final_Project.Event_Booking.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRequestDTO {
    @NotBlank(message = "Name is required!")
    private String name;
}
