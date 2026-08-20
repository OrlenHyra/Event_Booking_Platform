package com.Final_Project.Event_Booking.repository;

import com.Final_Project.Event_Booking.model.entity.Waitlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WaitlistRepository extends JpaRepository<Waitlist,Long> {
    boolean existsByAttendee_IdAndEvent_Id(Long attendeeId, Long eventId);

    @Query("""
    SELECT w FROM Waitlist w
    WHERE w.event.id = :eventId
    ORDER BY w.joinedAt ASC 
    """)
    List<Waitlist> findByEventInOrderByJoinedAtAsc(@Param("eventId") Long eventId);

}
