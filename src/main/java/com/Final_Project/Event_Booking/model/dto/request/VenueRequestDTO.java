package com.Final_Project.Event_Booking.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VenueRequestDTO {

    @NotBlank(message = "Venue name is required!")
    private String name;

    @NotBlank(message = "Address is required!")
    private String address;

    @NotBlank(message = "City is required!")
    private String city;

    @NotNull(message = "Capacity is required!")
    @Positive(message = "Capacity should be a positive number")
    private Integer capacity;
}
