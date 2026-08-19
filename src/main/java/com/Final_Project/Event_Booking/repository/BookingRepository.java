package com.Final_Project.Event_Booking.repository;

import com.Final_Project.Event_Booking.model.entity.Booking;
import com.Final_Project.Event_Booking.model.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking,Long> {
    boolean existsByBooker_IdAndEvent_IdAndStatus(Long bookerId, Long eventId, BookingStatus status);
}
