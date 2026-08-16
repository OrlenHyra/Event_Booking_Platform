package com.Final_Project.Event_Booking.repository;

import com.Final_Project.Event_Booking.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}
