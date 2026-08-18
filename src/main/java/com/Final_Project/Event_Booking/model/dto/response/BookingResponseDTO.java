package com.Final_Project.Event_Booking.model.dto.response;

import com.Final_Project.Event_Booking.model.enums.BookingStatus;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponseDTO {
    private Long id;

    private Integer seatsBooked;

    private BookingStatus status;

    private LocalDate bookingDate;

    private Long eventId;

    private String eventTitle;

    private Long userId;

    private String username;
}
