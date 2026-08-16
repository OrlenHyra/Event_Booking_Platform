package com.Final_Project.Event_Booking.model.entity;

import com.Final_Project.Event_Booking.model.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "booking")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id",nullable = false)
    private Long id;

    @Column(name = "seats_booked",nullable = false)
    private Integer seatsBooked;

    @Column(name = "status",nullable = false)
    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @Column(name = "booking_date")
    private LocalDateTime bookingDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(referencedColumnName = "id",name = "booker_id",nullable = false)
    private User booker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(referencedColumnName = "id",name = "event_id",nullable = false)
    private Event event;
}
