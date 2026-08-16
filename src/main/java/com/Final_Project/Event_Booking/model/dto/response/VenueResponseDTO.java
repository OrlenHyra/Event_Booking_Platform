package com.Final_Project.Event_Booking.model.dto.response;

import lombok.*;

@Builder
@AllArgsConstructor
@Getter
public class VenueResponseDTO {

    private Long id;

    private String name;

    private String address;

    private String city;

    private Integer capacity;
}
