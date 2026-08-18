package com.Final_Project.Event_Booking.repository;

import com.Final_Project.Event_Booking.model.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking,Long> {
}
