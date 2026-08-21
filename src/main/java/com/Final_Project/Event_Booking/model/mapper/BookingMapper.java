package com.Final_Project.Event_Booking.model.mapper;

import com.Final_Project.Event_Booking.model.dto.request.BookingRequestDTO;
import com.Final_Project.Event_Booking.model.dto.response.BookingResponseDTO;
import com.Final_Project.Event_Booking.model.entity.Booking;
import com.Final_Project.Event_Booking.model.entity.Event;
import com.Final_Project.Event_Booking.model.entity.Waitlist;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "booker", ignore = true)
    @Mapping(target = "bookingDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    Booking toEntity(BookingRequestDTO request);

    @Mapping(source = "event.id", target = "eventId")
    @Mapping(source = "event.title", target = "eventTitle")
    @Mapping(source = "booker.id", target = "userId")
    @Mapping(source = "booker.username", target = "username")
    @Mapping(target = "message", ignore = true)
    @Mapping(target = "waitlist", ignore = true)
    BookingResponseDTO toResponseDTO(Booking booking);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "booker", ignore = true)
    @Mapping(target = "bookingDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    void updateEntity(BookingRequestDTO request, @MappingTarget Booking booking);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "booker", source = "waitlist.attendee")
    @Mapping(target = "event", source = "event")
    @Mapping(target = "seatsBooked", source = "waitlist.seatsRequested")
    @Mapping(target = "status", constant = "CONFIRMED")
    @Mapping(target = "bookingDate", expression = "java(java.time.LocalDateTime.now())")
    Booking toBookingFromWaitlist(Waitlist waitlist, Event event);
}