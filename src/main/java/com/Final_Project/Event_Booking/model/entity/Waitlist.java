package com.Final_Project.Event_Booking.model.entity;

import com.Final_Project.Event_Booking.model.enums.WaitlistStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "waitlist",
        uniqueConstraints = {
            @UniqueConstraint(
                name = "uk_waitlist_user_event",
                columnNames = {"attendee_id","event_id"}
            )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Waitlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id",nullable = false,unique = true)
    private Long id;

    private LocalDateTime joinedAt;

    @Column(name = "seats_requested", nullable = false)
    private Integer seatsRequested;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private WaitlistStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id",referencedColumnName = "id",nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendee_id",referencedColumnName = "id",nullable = false)
    private User attendee;
}
