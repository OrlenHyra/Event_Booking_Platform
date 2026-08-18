package com.Final_Project.Event_Booking.repository;

import com.Final_Project.Event_Booking.model.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event,Long> {
}
