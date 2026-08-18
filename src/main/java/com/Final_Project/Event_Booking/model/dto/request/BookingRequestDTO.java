package com.Final_Project.Event_Booking.model.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequestDTO {
    @NotNull
    @Positive
    private Integer seatsBooked;

    @NotNull
    private Long eventId;
}
