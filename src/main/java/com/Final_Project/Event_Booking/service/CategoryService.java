package com.Final_Project.Event_Booking.service;

import com.Final_Project.Event_Booking.model.dto.request.CategoryRequestDTO;
import com.Final_Project.Event_Booking.model.dto.response.CategoryResponseDTO;

import java.util.List;

public interface CategoryService {

    CategoryResponseDTO createCategory(CategoryRequestDTO request);

    CategoryResponseDTO getCategory(Long id);

    List<CategoryResponseDTO> getAllCategories();

    CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO request);

    void deleteCategory(Long id);
}
