package com.Final_Project.Event_Booking.repository;

import com.Final_Project.Event_Booking.model.entity.Waitlist;
import com.Final_Project.Event_Booking.model.enums.WaitlistStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WaitlistRepository extends JpaRepository<Waitlist,Long> {
    boolean existsByAttendee_IdAndEvent_Id(Long attendeeId, Long eventId);

    @Query("""
    SELECT w FROM Waitlist w
    WHERE w.event.id = :eventId
    AND w.status = :status
    ORDER BY w.joinedAt ASC 
    """)
    List<Waitlist> findByEventInOrderByJoinedAtAsc(@Param("eventId") Long eventId,@Param("status") WaitlistStatus status);

    List<Waitlist> findByAttendee_Id(Long attendeeId);

    List<Waitlist> findByAttendee_IdAndStatus(Long attendeeId, WaitlistStatus status);

    List<Waitlist> findByStatus(WaitlistStatus status);

    List<Waitlist> findByEvent_Id(Long eventId);
}
