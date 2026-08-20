package com.Final_Project.Event_Booking.repository;

import com.Final_Project.Event_Booking.model.entity.Booking;
import com.Final_Project.Event_Booking.model.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking,Long> {
    boolean existsByBooker_IdAndEvent_IdAndStatus(Long bookerId, Long eventId, BookingStatus status);

    List<Booking> findByBooker_Id(Long bookerId);

    List<Booking> findByBooker_IdAndStatus(Long bookerId,BookingStatus status);

    @Query("""
    SELECT b
    FROM Booking b
    JOIN b.event e
    JOIN e.organizer o
    WHERE e.id = :eventId
    AND o.id = :organizerId
""")
    List<Booking> findBookingsByEventAndOrganizer(
            @Param("eventId") Long eventId,
            @Param("organizerId") Long organizerId
    );
}
