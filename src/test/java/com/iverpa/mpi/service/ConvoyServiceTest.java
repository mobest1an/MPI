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

    @Test
    void getActiveConvoy_ShouldReturnConvoyResponse_WhenConvoyExists() {
        // Arrange
        User recruitUser = new User();
        recruitUser.setId(2L);
        recruitUser.setUsername("recruit1");

        Summon summon = new Summon();
        summon.setId(1L);
        summon.setUser(recruitUser);
        summon.setMilitaryBranch("Пехота");

        when(convoyRepository.findByEscort(escort)).thenReturn(Optional.of(convoy));
        when(summonService.findAllByConvoy(convoy)).thenReturn(List.of(summon));

        // Act
        var result = convoyService.getActiveConvoy(escort);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(convoy.getId(), result.get().convoyId());
        assertEquals(1, result.get().recruits().size());
        assertEquals("recruit1", result.get().recruits().get(0).username());
        assertEquals("Пехота", result.get().recruits().get(0).militaryBranch());
        verify(convoyRepository).findByEscort(escort);
        verify(summonService).findAllByConvoy(convoy);
    }

    @Test
    void getActiveConvoy_ShouldReturnEmpty_WhenConvoyDoesNotExist() {
        // Arrange
        when(convoyRepository.findByEscort(escort)).thenReturn(Optional.empty());

        // Act
        var result = convoyService.getActiveConvoy(escort);

        // Assert
        assertFalse(result.isPresent());
        verify(convoyRepository).findByEscort(escort);
        verify(summonService, never()).findAllByConvoy(any(Convoy.class));
    }

    @Test
    void createConvoy_ShouldCreateConvoyAndUpdateSummons_WhenValidRequest() {
        // Arrange
        User recruitUser = new User();
        recruitUser.setId(2L);
        recruitUser.setUsername("recruit1");

        Summon summon = new Summon();
        summon.setId(1L);
        summon.setUser(recruitUser);
        summon.setStatus(RecruitStatus.WAITING_ESCORT);
        summon.setMilitaryBranch("Пехота");

        List<Long> summonIds = List.of(1L);

        when(convoyRepository.existsByEscort(escort)).thenReturn(false);
        when(summonService.findAllByIdsAndStatus(summonIds, RecruitStatus.WAITING_ESCORT)).thenReturn(List.of(summon));
        when(convoyRepository.save(any(Convoy.class))).thenAnswer(invocation -> {
            Convoy saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        // Act
        var result = convoyService.createConvoy(escort, summonIds);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.convoyId());
        assertEquals(1, result.recruits().size());
        assertEquals("recruit1", result.recruits().get(0).username());
        assertEquals(RecruitStatus.IN_CONVOY, summon.getStatus());
        assertNotNull(summon.getConvoy());
        verify(convoyRepository).existsByEscort(escort);
        verify(summonService).findAllByIdsAndStatus(summonIds, RecruitStatus.WAITING_ESCORT);
        verify(convoyRepository).save(any(Convoy.class));
        verify(summonService).saveAll(anyList());
    }

    @Test
    void createConvoy_ShouldThrowException_WhenEscortAlreadyHasConvoy() {
        // Arrange
        when(convoyRepository.existsByEscort(escort)).thenReturn(true);

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> convoyService.createConvoy(escort, List.of(1L))
        );
        assertEquals("У вас уже есть активный конвой", exception.getMessage());
        verify(convoyRepository).existsByEscort(escort);
        verify(summonService, never()).findAllByIdsAndStatus(anyList(), any(RecruitStatus.class));
        verify(convoyRepository, never()).save(any(Convoy.class));
    }

    @Test
    void createConvoy_ShouldThrowException_WhenSummonIdsIsEmpty() {
        // Arrange
        when(convoyRepository.existsByEscort(escort)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> convoyService.createConvoy(escort, List.of())
        );
        assertEquals("Необходимо выбрать хотя бы одного призывника", exception.getMessage());
        verify(convoyRepository).existsByEscort(escort);
        verify(summonService, never()).findAllByIdsAndStatus(anyList(), any(RecruitStatus.class));
    }

    @Test
    void createConvoy_ShouldThrowException_WhenSummonIdsIsNull() {
        // Arrange
        when(convoyRepository.existsByEscort(escort)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> convoyService.createConvoy(escort, null)
        );
        assertEquals("Необходимо выбрать хотя бы одного призывника", exception.getMessage());
        verify(convoyRepository).existsByEscort(escort);
        verify(summonService, never()).findAllByIdsAndStatus(anyList(), any(RecruitStatus.class));
    }

    @Test
    void createConvoy_ShouldThrowException_WhenSomeSummonsNotAvailable() {
        // Arrange
        List<Long> summonIds = List.of(1L, 2L);

        User recruitUser = new User();
        recruitUser.setId(2L);
        recruitUser.setUsername("recruit1");

        Summon summon = new Summon();
        summon.setId(1L);
        summon.setUser(recruitUser);
        summon.setStatus(RecruitStatus.WAITING_ESCORT);

        when(convoyRepository.existsByEscort(escort)).thenReturn(false);
        // Возвращаем только 1 summon вместо 2
        when(summonService.findAllByIdsAndStatus(summonIds, RecruitStatus.WAITING_ESCORT)).thenReturn(List.of(summon));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> convoyService.createConvoy(escort, summonIds)
        );
        assertEquals("Некоторые призывники уже недоступны для конвоирования", exception.getMessage());
        verify(convoyRepository).existsByEscort(escort);
        verify(summonService).findAllByIdsAndStatus(summonIds, RecruitStatus.WAITING_ESCORT);
        verify(convoyRepository, never()).save(any(Convoy.class));
    }
}
