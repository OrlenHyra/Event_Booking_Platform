package com.Final_Project.Event_Booking.service.impl;

import com.Final_Project.Event_Booking.exception.custom.ResourcesNotFoundException;
import com.Final_Project.Event_Booking.model.dto.request.VenueRequestDTO;
import com.Final_Project.Event_Booking.model.dto.response.VenueResponseDTO;
import com.Final_Project.Event_Booking.model.entity.Venue;
import com.Final_Project.Event_Booking.model.mapper.VenueMapper;
import com.Final_Project.Event_Booking.repository.VenueRepository;
import com.Final_Project.Event_Booking.service.VenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VenueServiceImpl implements VenueService {
    private final VenueRepository venueRepository;
    private final VenueMapper venueMapper;

    @Override
    public VenueResponseDTO createVenue(VenueRequestDTO request) {
        Venue venue =venueMapper.toEntity(request);
        Venue savedVenue=venueRepository.save(venue);
        return venueMapper.toResponseDTO(savedVenue);
    }

    @Override
    public List<VenueResponseDTO> getAllVenues() {
        return venueRepository.findAll()
                .stream()
                .map(venueMapper::toResponseDTO)
                .toList();
    }

    @Override
    public VenueResponseDTO getVenue(Long id) {
        Venue venue=venueRepository.findById(id)
                .orElseThrow(()->new ResourcesNotFoundException("Venue with id:"+id+" not found"));
        return venueMapper.toResponseDTO(venue);
    }

    @Override
    public VenueResponseDTO updateVenue(Long id, VenueRequestDTO request) {
        Venue venue=venueRepository.findById(id)
                .orElseThrow(()->new ResourcesNotFoundException("Venue with id:"+id+" not found"));
        venueMapper.updateEntity(request,venue);
        Venue updatedVenue=venueRepository.save(venue);
        return venueMapper.toResponseDTO(updatedVenue);
    }

    @Override
    public void deleteVenue(Long id) {
        Venue venue=venueRepository.findById(id)
                .orElseThrow(()->new ResourcesNotFoundException("Venue with id:"+id+" not found"));
        venueRepository.delete(venue);
    }
}
