package com.Final_Project.Event_Booking.repository;

import com.Final_Project.Event_Booking.model.entity.Event;
import com.Final_Project.Event_Booking.model.enums.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event,Long> {
    Page<Event> findByStatusInAndCategories_Name(List<EventStatus> statuses, String categoryName, Pageable pageable);

    Page<Event> findByStatusInAndVenue_City(List<EventStatus> statuses, String venueCity, Pageable pageable);

    @Query("""
        SELECT e FROM Event e
        WHERE e.status IN :statuses
        AND e.startDateTime BETWEEN :startDateTime AND :endDateTime
    """)
    Page<Event> findEventByDateRange(
            @Param("statuses") List<EventStatus> statuses,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            Pageable pageable
            );

    @Query(value = """
    SELECT * FROM Event
    WHERE status IN ('ACTIVE','COMPLETED','UPCOMING')
    AND price BETWEEN :minPrice AND :maxPrice
    """,nativeQuery = true)
    Page<Event> findEventByPriceRange(
            @Param("minPrice")BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
            );

    List<Event> findByStatusIn(List<EventStatus> statuses);

    List<Event> findByOrganizer_Id(Long organizerId);
}
