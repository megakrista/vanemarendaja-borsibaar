package com.borsibaar.service;

import com.borsibaar.dto.BarStationRequestDto;
import com.borsibaar.dto.BarStationResponseDto;
import com.borsibaar.entity.BarStation;
import com.borsibaar.entity.User;
import com.borsibaar.exception.BadRequestException;
import com.borsibaar.exception.DuplicateResourceException;
import com.borsibaar.exception.NotFoundException;
import com.borsibaar.mapper.BarStationMapper;
import com.borsibaar.repository.BarStationRepository;
import com.borsibaar.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BarStationServiceTest {

    @Mock private BarStationRepository barStationRepository;
    @Mock private UserRepository userRepository;
    @Mock private BarStationMapper barStationMapper;

    @InjectMocks private BarStationService barStationService;

    @Test
    void createStation_Success_AssignsUsers() {
        UUID uId = UUID.randomUUID();
        BarStationRequestDto request = new BarStationRequestDto("Main", "Desc", true, List.of(uId));
        when(barStationRepository.findByOrganizationId(1L)).thenReturn(List.of());
        User user = new User(); user.setId(uId); user.setOrganizationId(1L); user.setBarStations(new HashSet<>()); user.setName("User");
        when(userRepository.findById(uId)).thenReturn(Optional.of(user));
        BarStation saved = BarStation.builder().id(5L).name("Main").organizationId(1L).users(new HashSet<>()).build();
        when(barStationRepository.save(any(BarStation.class))).thenReturn(saved);
        when(barStationMapper.toResponseDto(saved)).thenReturn(new BarStationResponseDto(5L, 1L, "Main", "Desc", true, List.of(), null, null));

        BarStationResponseDto dto = barStationService.createStation(1L, request);
        assertEquals("Main", dto.name());
        verify(barStationRepository).save(any(BarStation.class));
    }

    @Test
    void createStation_DuplicateName_Throws() {
        BarStation existing = BarStation.builder().id(1L).name("Main").organizationId(1L).build();
        when(barStationRepository.findByOrganizationId(1L)).thenReturn(List.of(existing));
        BarStationRequestDto request = new BarStationRequestDto("Main", null, null, null);
        assertThrows(DuplicateResourceException.class, () -> barStationService.createStation(1L, request));
    }

    @Test
    void updateStation_DuplicateName_Throws() {
        BarStation station = BarStation.builder().id(2L).name("StationA").organizationId(1L).users(new HashSet<>()).build();
        BarStation other = BarStation.builder().id(3L).name("Main").organizationId(1L).build();
        when(barStationRepository.findByOrganizationIdAndId(1L, 2L)).thenReturn(Optional.of(station));
        when(barStationRepository.findByOrganizationId(1L)).thenReturn(List.of(station, other));
        BarStationRequestDto request = new BarStationRequestDto("Main", null, null, null);
        assertThrows(DuplicateResourceException.class, () -> barStationService.updateStation(1L, 2L, request));
    }

    @Test
    void deleteStation_NotFound_Throws() {
        when(barStationRepository.findByOrganizationIdAndId(1L, 9L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> barStationService.deleteStation(1L, 9L));
    }

    @Test
    void getUserStations_UserOrgMismatch_Throws() {
        UUID uid = UUID.randomUUID();
        User user = new User(); user.setId(uid); user.setOrganizationId(2L); user.setBarStations(new HashSet<>());
        when(userRepository.findById(uid)).thenReturn(Optional.of(user));
        assertThrows(BadRequestException.class, () -> barStationService.getUserStations(uid, 1L));
    }

    @Test
    void getStationById_NotFound_Throws() {
        when(barStationRepository.findByOrganizationIdAndId(1L, 55L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> barStationService.getStationById(1L, 55L));
    }
}
