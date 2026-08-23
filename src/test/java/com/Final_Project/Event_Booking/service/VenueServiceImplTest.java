package com.Final_Project.Event_Booking.service;

import com.Final_Project.Event_Booking.exception.custom.ResourcesNotFoundException;
import com.Final_Project.Event_Booking.model.dto.request.VenueRequestDTO;
import com.Final_Project.Event_Booking.model.dto.response.VenueResponseDTO;
import com.Final_Project.Event_Booking.model.entity.Venue;
import com.Final_Project.Event_Booking.model.mapper.VenueMapper;
import com.Final_Project.Event_Booking.repository.VenueRepository;
import com.Final_Project.Event_Booking.service.impl.VenueServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VenueServiceImplTest {

    @Mock
    private VenueRepository venueRepository;

    @Mock
    private VenueMapper venueMapper;

    @InjectMocks
    private VenueServiceImpl venueService;

    private VenueRequestDTO requestDTO;
    private Venue venue;

    @BeforeEach
    void setUp() {
        requestDTO = VenueRequestDTO.builder()
                .name("test venue")
                .address("test address")
                .city("test city")
                .capacity(100)
                .build();

        venue = Venue.builder()
                .id(1L)
                .name("test venue")
                .address("test address")
                .city("test city")
                .capacity(100)
                .build();
    }

    @Test
    void createsVenueSuccessfully() {
        VenueResponseDTO expectedResponse = VenueResponseDTO.builder()
                .id(1L)
                .name("test venue")
                .address("test address")
                .city("test city")
                .capacity(100)
                .build();

        when(venueMapper.toEntity(requestDTO)).thenReturn(venue);
        when(venueRepository.save(venue)).thenReturn(venue);
        when(venueMapper.toResponseDTO(venue)).thenReturn(expectedResponse);

        VenueResponseDTO response = venueService.createVenue(requestDTO);

        assertThat(response).isEqualTo(expectedResponse);
        verify(venueRepository).save(venue);
    }

    @Test
    void getsAllVenues() {
        Venue secondVenue = Venue.builder()
                .id(2L)
                .name("test venue two")
                .address("test address two")
                .city("test city two")
                .capacity(50)
                .build();

        VenueResponseDTO firstResponse = VenueResponseDTO.builder()
                .id(1L)
                .name("test venue")
                .build();

        VenueResponseDTO secondResponse = VenueResponseDTO.builder()
                .id(2L)
                .name("test venue two")
                .build();

        when(venueRepository.findAll()).thenReturn(List.of(venue, secondVenue));
        when(venueMapper.toResponseDTO(venue)).thenReturn(firstResponse);
        when(venueMapper.toResponseDTO(secondVenue)).thenReturn(secondResponse);

        List<VenueResponseDTO> responses = venueService.getAllVenues();

        assertThat(responses).containsExactly(firstResponse, secondResponse);
    }

    @Test
    void getsVenueById() {
        VenueResponseDTO expectedResponse = VenueResponseDTO.builder()
                .id(1L)
                .name("test venue")
                .build();

        when(venueRepository.findById(1L)).thenReturn(Optional.of(venue));
        when(venueMapper.toResponseDTO(venue)).thenReturn(expectedResponse);

        VenueResponseDTO response = venueService.getVenue(1L);

        assertThat(response).isEqualTo(expectedResponse);
    }

    @Test
    void cannotGetVenueThatDoesNotExist() {
        when(venueRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> venueService.getVenue(99L))
                .isInstanceOf(ResourcesNotFoundException.class);
    }

    @Test
    void updatesVenueSuccessfully() {
        VenueRequestDTO updateRequest = VenueRequestDTO.builder()
                .name("updated test venue")
                .address("updated test address")
                .city("updated test city")
                .capacity(200)
                .build();

        VenueResponseDTO expectedResponse = VenueResponseDTO.builder()
                .id(1L)
                .name("updated test venue")
                .build();

        when(venueRepository.findById(1L)).thenReturn(Optional.of(venue));
        when(venueRepository.save(venue)).thenReturn(venue);
        when(venueMapper.toResponseDTO(venue)).thenReturn(expectedResponse);

        VenueResponseDTO response =
                venueService.updateVenue(1L, updateRequest);

        assertThat(response).isEqualTo(expectedResponse);
        verify(venueMapper).updateEntity(updateRequest, venue);
    }

    @Test
    void deletesVenueSuccessfully() {
        when(venueRepository.findById(1L)).thenReturn(Optional.of(venue));

        venueService.deleteVenue(1L);

        verify(venueRepository).delete(venue);
    }
}