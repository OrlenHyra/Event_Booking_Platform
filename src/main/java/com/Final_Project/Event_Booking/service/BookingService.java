package com.Final_Project.Event_Booking.service;

import com.Final_Project.Event_Booking.model.dto.request.BookingRequestDTO;
import com.Final_Project.Event_Booking.model.dto.response.BookingCreationResponseDTO;
import com.Final_Project.Event_Booking.model.dto.response.BookingResponseDTO;
import com.Final_Project.Event_Booking.model.enums.BookingStatus;
import com.Final_Project.Event_Booking.repository.BookingRepository;

import java.util.List;

public interface BookingService {
    BookingCreationResponseDTO createBooking(BookingRequestDTO request);

    BookingResponseDTO getBooking(Long id);

    List<BookingResponseDTO> getAllBookings();

    void cancelBooking(Long id);

    void cancelMyBooking(Long id);

    List<BookingResponseDTO> getMyBookings(BookingStatus status);

    List<BookingResponseDTO> getMyEventBookings(Long eventId);
}
