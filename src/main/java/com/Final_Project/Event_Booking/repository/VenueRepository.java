package com.Final_Project.Event_Booking.repository;

import com.Final_Project.Event_Booking.model.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VenueRepository extends JpaRepository<Venue,Long> {
}
