package com.Final_Project.Event_Booking.model.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingCreationResponseDTO {

    private BookingResponseDTO booking;

    private WaitlistResponseDTO waitlist;
}