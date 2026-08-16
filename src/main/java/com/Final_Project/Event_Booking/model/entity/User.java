package com.Final_Project.Event_Booking.model.entity;

import com.Final_Project.Event_Booking.model.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id",nullable = false)
    private Long id;

    @Column(name = "username",nullable = false,unique = true)
    private String username;

    @Column(name = "password",nullable = false)
    private String password;

    @Column(name = "email",nullable = false,unique = true)
    private String email;

    @Column(name = "role",nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @OneToMany(mappedBy = "organizer")
    private List<Event> events=new ArrayList<>();

    @OneToMany(mappedBy = "booker")
    private List<Booking> bookings=new ArrayList<>();

    @OneToMany(mappedBy = "reviewer")
    private List<Review> reviews=new ArrayList<>();
}
