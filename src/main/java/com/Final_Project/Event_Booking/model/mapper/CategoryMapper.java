package com.Final_Project.Event_Booking.model.mapper;

import com.Final_Project.Event_Booking.model.dto.request.CategoryRequestDTO;
import com.Final_Project.Event_Booking.model.dto.response.CategoryResponseDTO;
import com.Final_Project.Event_Booking.model.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "id",ignore = true)
    Category toEntity(CategoryRequestDTO request);

    CategoryResponseDTO toResponseDTO(Category category);

    @Mapping(target = "id",ignore = true)
    void updateEntity(CategoryRequestDTO request, @MappingTarget Category category);
}
