package com.Final_Project.Event_Booking.model.mapper;

import com.Final_Project.Event_Booking.model.dto.response.WaitlistResponseDTO;
import com.Final_Project.Event_Booking.model.entity.Event;
import com.Final_Project.Event_Booking.model.entity.User;
import com.Final_Project.Event_Booking.model.entity.Waitlist;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WaitlistMapper {
    @Mapping(target = "attendeeId", source = "attendee.id")
    @Mapping(target = "eventId", source = "event.id")
    WaitlistResponseDTO toResponseDTO(Waitlist waitlist);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "attendee", source = "user")
    @Mapping(target = "event", source = "event")
    @Mapping(target = "seatsRequested", source = "seatsRequested")
    Waitlist toEntity(User user, Event event, Integer seatsRequested);
}
