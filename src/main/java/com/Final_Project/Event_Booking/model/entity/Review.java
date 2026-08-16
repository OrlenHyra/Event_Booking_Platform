package com.Final_Project.Event_Booking.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "review",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_review_user_event",
                        columnNames = {"reviewer_id", "event_id"}
                )
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id",nullable = false)
    private Long id;

    @Column(name = "rating",nullable = false)
    private Integer rating;

    @Column(name = "comment",nullable = false)
    private String comment;

    @Column(name = "created_at",nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(referencedColumnName = "id",name = "reviewer_id",nullable = false)
    private User reviewer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(referencedColumnName = "id",name = "event_id",nullable = false)
    private Event event;
}
