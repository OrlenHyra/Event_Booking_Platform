package com.Final_Project.Event_Booking.model.entity;

import com.Final_Project.Event_Booking.model.enums.EventStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "event")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id",nullable = false)
    private Long id;

    @Column(name = "title",nullable = false)
    private String title;

    @Column(name = "description",nullable = false)
    private String description;

    @Column(name = "start_date_time",nullable = false)
    private LocalDateTime startDateTime;

    @Column(name = "end_date_time",nullable = false)
    private LocalDateTime endDateTime;

    @Column(name = "price",nullable = false)
    private BigDecimal price;

    @Column(name = "total_seats",nullable = false)
    private Integer totalSeats;

    @Column(name = "available_seats",nullable = false)
    private Integer availableSeats;

    @Column(name = "status",nullable = false)
    @Enumerated(EnumType.STRING)
    private EventStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(referencedColumnName = "id", name = "organizer_id",nullable = false)
    private User organizer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(referencedColumnName = "id",name = "venue_id",nullable = false)
    private Venue venue;

    @OneToMany(mappedBy = "event",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<Booking> bookings=new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "event_categories",
            joinColumns = @JoinColumn(referencedColumnName = "id",name = "event_id"),
            inverseJoinColumns = @JoinColumn(referencedColumnName = "id",name = "category_id")
    )
    private List<Category> categories = new ArrayList<>();

    @OneToMany(mappedBy = "event",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<Review> reviews=new ArrayList<>();
}
