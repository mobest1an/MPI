package com.iverpa.mpi.service;

import com.iverpa.mpi.dao.SummonService;
import com.iverpa.mpi.dao.repository.ConvoyRepository;
import com.iverpa.mpi.dao.repository.ComplaintRepository;
import com.iverpa.mpi.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConvoyServiceTest {

    @Mock
    private ConvoyRepository convoyRepository;

    @Mock
    private SummonService summonService;

    @Mock
    private ComplaintRepository complaintRepository;

    @InjectMocks
    private ConvoyService convoyService;

    private User escort;
    private Convoy convoy;

    @BeforeEach
    void setUp() {
        escort = new User();
        escort.setId(1L);
        escort.setUsername("escortUser");

        convoy = new Convoy();
        convoy.setId(1L);
        convoy.setEscort(escort);
    }

    @Test
    void hasActiveConvoy_ShouldReturnTrue_WhenConvoyExists() {
        // Arrange
        when(convoyRepository.existsByEscort(escort)).thenReturn(true);

        // Act
        boolean result = convoyService.hasActiveConvoy(escort);

        // Assert
        assertTrue(result);
        verify(convoyRepository).existsByEscort(escort);
    }

    @Test
    void hasActiveConvoy_ShouldReturnFalse_WhenConvoyDoesNotExist() {
        // Arrange
        when(convoyRepository.existsByEscort(escort)).thenReturn(false);

        // Act
        boolean result = convoyService.hasActiveConvoy(escort);

        // Assert
        assertFalse(result);
        verify(convoyRepository).existsByEscort(escort);
    }

    @Test
    void dismissConvoy_ShouldUpdateSummonStatusesAndDeleteConvoy() {
        // Arrange
        Summon summon1 = new Summon();
        summon1.setId(1L);
        summon1.setConvoy(convoy);
        summon1.setStatus(RecruitStatus.IN_CONVOY);

        Summon summon2 = new Summon();
        summon2.setId(2L);
        summon2.setConvoy(convoy);
        summon2.setStatus(RecruitStatus.IN_CONVOY);

        when(convoyRepository.findByEscort(escort)).thenReturn(Optional.of(convoy));
        when(summonService.findAllByConvoy(convoy)).thenReturn(List.of(summon1, summon2));
        doNothing().when(summonService).saveAll(anyList());

        // Act
        convoyService.dismissConvoy(escort);

        // Assert
        assertEquals(RecruitStatus.DONE, summon1.getStatus());
        assertNull(summon1.getConvoy());
        assertEquals(RecruitStatus.DONE, summon2.getStatus());
        assertNull(summon2.getConvoy());
        verify(convoyRepository).findByEscort(escort);
        verify(summonService).findAllByConvoy(convoy);
        verify(summonService).saveAll(anyList());
        verify(complaintRepository).deleteAllByConvoy(convoy);
        verify(convoyRepository).delete(convoy);
    }

    @Test
    void dismissConvoy_ShouldThrowException_WhenConvoyDoesNotExist() {
        // Arrange
        when(convoyRepository.findByEscort(escort)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> convoyService.dismissConvoy(escort)
        );
        assertEquals("У вас нет активного конвоя", exception.getMessage());
        verify(convoyRepository).findByEscort(escort);
        verify(summonService, never()).findAllByConvoy(any(Convoy.class));
        verify(summonService, never()).saveAll(anyList());
        verify(convoyRepository, never()).delete(any(Convoy.class));
    }

    @Test
    void getComplaintsCount_ShouldReturnCount_WhenConvoyExists() {
        // Arrange
        int expectedCount = 5;
        when(convoyRepository.findByEscort(escort)).thenReturn(Optional.of(convoy));
        when(complaintRepository.countByConvoy(convoy)).thenReturn(expectedCount);

        // Act
        int result = convoyService.getComplaintsCount(escort);

        // Assert
        assertEquals(expectedCount, result);
        verify(convoyRepository).findByEscort(escort);
        verify(complaintRepository).countByConvoy(convoy);
    }

    @Test
    void getComplaintsCount_ShouldReturnZero_WhenConvoyDoesNotExist() {
        // Arrange
        when(convoyRepository.findByEscort(escort)).thenReturn(Optional.empty());

        // Act
        int result = convoyService.getComplaintsCount(escort);

        // Assert
        assertEquals(0, result);
        verify(convoyRepository).findByEscort(escort);
        verify(complaintRepository, never()).countByConvoy(any(Convoy.class));
    }
}
