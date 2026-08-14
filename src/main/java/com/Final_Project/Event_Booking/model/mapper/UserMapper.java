package com.Final_Project.Event_Booking.model.mapper;

import com.Final_Project.Event_Booking.model.dto.request.UserRequestDTO;
import com.Final_Project.Event_Booking.model.dto.response.UserResponseDTO;
import com.Final_Project.Event_Booking.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    User toEntity (UserRequestDTO userRequestDTO);

    UserResponseDTO toResponseDTO(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateEntity(UserRequestDTO request, @MappingTarget User user);
}
