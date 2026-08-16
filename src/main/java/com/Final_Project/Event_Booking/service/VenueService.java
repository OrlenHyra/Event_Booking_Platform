package com.Final_Project.Event_Booking.service;

import com.Final_Project.Event_Booking.model.dto.request.VenueRequestDTO;
import com.Final_Project.Event_Booking.model.dto.response.VenueResponseDTO;

import java.util.List;

public interface VenueService {
    VenueResponseDTO createVenue(VenueRequestDTO request);

    List<VenueResponseDTO> getAllVenues();

    VenueResponseDTO getVenue(Long id);

    VenueResponseDTO updateVenue(Long id, VenueRequestDTO request);

    void deleteVenue(Long id);
}
