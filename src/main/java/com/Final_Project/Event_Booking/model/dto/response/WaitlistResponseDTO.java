package com.Final_Project.Event_Booking.model.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaitlistResponseDTO {
    private Long id;

    private Long attendeeId;

    private Long eventId;

    private Integer seatsRequested;

    private LocalDateTime joinedAt;

}
