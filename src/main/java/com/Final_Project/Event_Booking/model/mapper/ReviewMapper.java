package com.Final_Project.Event_Booking.model.mapper;

import com.Final_Project.Event_Booking.model.dto.request.ReviewRequestDTO;
import com.Final_Project.Event_Booking.model.dto.response.ReviewResponseDTO;
import com.Final_Project.Event_Booking.model.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ReviewMapper {
    @Mapping(target = "id",ignore = true)
    @Mapping(target = "createdAt",ignore = true)
    @Mapping(target = "reviewer",ignore = true)
    @Mapping(target = "event",ignore = true)
    Review toEntity(ReviewRequestDTO request);

    @Mapping(target = "id",ignore = true)
    @Mapping(target = "createdAt",ignore = true)
    @Mapping(target = "reviewer",ignore = true)
    @Mapping(target = "event",ignore = true)
    void updateEntity(ReviewRequestDTO request, @MappingTarget Review review);

    @Mapping(target = "eventId",source = "event.id")
    @Mapping(target = "eventTitle",source = "event.title")
    @Mapping(target = "userId",source = "reviewer.id")
    @Mapping(target = "username",source = "reviewer.username")
    ReviewResponseDTO toResponseDTO(Review review);
}
