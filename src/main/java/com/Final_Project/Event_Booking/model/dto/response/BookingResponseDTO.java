package com.Final_Project.Event_Booking.model.dto.response;

import com.Final_Project.Event_Booking.model.enums.BookingStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponseDTO {
    private Long id;

    private Integer seatsBooked;

    private BookingStatus status;

    private LocalDateTime bookingDate;

    private Long eventId;

    private String eventTitle;

    private Long userId;

    private String username;

    private String message;

    private WaitlistResponseDTO waitlist;
}