package com.Final_Project.Event_Booking.model.mapper;

import com.Final_Project.Event_Booking.model.dto.request.VenueRequestDTO;
import com.Final_Project.Event_Booking.model.dto.response.VenueResponseDTO;
import com.Final_Project.Event_Booking.model.entity.Venue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface VenueMapper {

    @Mapping(target = "id",ignore = true)
    Venue toEntity(VenueRequestDTO request);

    VenueResponseDTO toResponseDTO(Venue venue);

    @Mapping(target = "id",ignore = true)
    void updateEntity(VenueRequestDTO request, @MappingTarget Venue venue);
}
