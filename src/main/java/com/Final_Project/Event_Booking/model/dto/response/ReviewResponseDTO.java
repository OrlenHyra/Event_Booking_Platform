package com.Final_Project.Event_Booking.model.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponseDTO {
    private Long id;

    private Integer rating;

    private String comment;

    private LocalDateTime createdAt;

    private Long eventId;

    private String eventTitle;

    private Long userId;

    private String username;
}
