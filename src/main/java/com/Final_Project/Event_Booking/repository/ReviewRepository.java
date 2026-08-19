package com.Final_Project.Event_Booking.repository;

import com.Final_Project.Event_Booking.model.entity.Review;
import com.Final_Project.Event_Booking.model.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review,Long> {
    boolean existsByReviewer_IdAndEvent_Id(Long reviewerId, Long eventId);

}
