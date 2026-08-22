package com.Final_Project.Event_Booking.service.impl;

import com.Final_Project.Event_Booking.exception.custom.ResourcesNotFoundException;
import com.Final_Project.Event_Booking.model.dto.request.VenueRequestDTO;
import com.Final_Project.Event_Booking.model.dto.response.VenueResponseDTO;
import com.Final_Project.Event_Booking.model.entity.Venue;
import com.Final_Project.Event_Booking.model.mapper.VenueMapper;
import com.Final_Project.Event_Booking.repository.VenueRepository;
import com.Final_Project.Event_Booking.service.VenueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VenueServiceImpl implements VenueService {
    private final VenueRepository venueRepository;
    private final VenueMapper venueMapper;

    @Override
    public VenueResponseDTO createVenue(VenueRequestDTO request) {
        log.info("Creating new venue");
        Venue venue =venueMapper.toEntity(request);
        Venue savedVenue=venueRepository.save(venue);
        log.info("Venue created successfully with id: {}", savedVenue.getId());
        return venueMapper.toResponseDTO(savedVenue);
    }

    @Override
    public List<VenueResponseDTO> getAllVenues() {
        log.info("Fetching all venues");
        return venueRepository.findAll()
                .stream()
                .map(venueMapper::toResponseDTO)
                .toList();
    }

    @Override
    public VenueResponseDTO getVenue(Long id) {
        log.info("Fetching venue with id: {}", id);
        Venue venue=venueRepository.findById(id)
                .orElseThrow(()->new ResourcesNotFoundException("Venue with id:"+id+" not found"));
        log.info("Venue with id: {} found successfully", id);
        return venueMapper.toResponseDTO(venue);
    }

    @Override
    public VenueResponseDTO updateVenue(Long id, VenueRequestDTO request) {
        log.info("Updating venue with id: {}", id);
        Venue venue=venueRepository.findById(id)
                .orElseThrow(()->new ResourcesNotFoundException("Venue with id:"+id+" not found"));
        venueMapper.updateEntity(request,venue);
        Venue updatedVenue=venueRepository.save(venue);
        log.info("Venue with id: {} updated successfully", updatedVenue.getId());
        return venueMapper.toResponseDTO(updatedVenue);
    }

    @Override
    public void deleteVenue(Long id) {
        log.info("Deleting venue with id: {}", id);
        Venue venue=venueRepository.findById(id)
                .orElseThrow(()->new ResourcesNotFoundException("Venue with id:"+id+" not found"));
        venueRepository.delete(venue);
        log.info("Venue with id: {} deleted successfully", id);
    }
}
