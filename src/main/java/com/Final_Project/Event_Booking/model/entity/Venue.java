package com.Final_Project.Event_Booking.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "venue")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id",nullable = false)
    private Long id;

    @Column(name = "name",nullable = false)
    private String name;

    @Column(name = "address",nullable = false)
    private String address;

    @Column(name = "city",nullable = false)
    private String city;

    @Column(name = "capacity",nullable = false)
    private Integer capacity;

    @OneToMany(mappedBy = "venue",orphanRemoval = true,cascade = CascadeType.ALL)
    private List<Event> events=new ArrayList<>();
}
