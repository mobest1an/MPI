package com.iverpa.mpi.service;

import com.iverpa.mpi.dao.SummonService;
import com.iverpa.mpi.model.RecruitStatus;
import com.iverpa.mpi.model.Summon;
import com.iverpa.mpi.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ElectronicQueueServiceTest {

    @Mock
    private SummonService summonService;

    @InjectMocks
    private ElectronicQueueService electronicQueueService;

    private User user;
    private Summon summon;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testUser");

        summon = new Summon();
        summon.setId(1L);
        summon.setUser(user);
        summon.setStatus(RecruitStatus.NOT_STARTED);
    }

    @Test
    void join_ShouldSetStatusToInQueue_WhenStatusIsNotStarted() {
        // Arrange
        when(summonService.findByUserId(anyLong())).thenReturn(summon);
        when(summonService.save(any(Summon.class))).thenReturn(summon);

        // Act
        electronicQueueService.join(user);

        // Assert
        assertEquals(RecruitStatus.IN_QUEUE, summon.getStatus());
        verify(summonService).findByUserId(user.getId());
        verify(summonService).save(summon);
    }

    @Test
    void join_ShouldThrowException_WhenStatusIsNotNotStarted() {
        // Arrange
        summon.setStatus(RecruitStatus.IN_QUEUE);
        when(summonService.findByUserId(anyLong())).thenReturn(summon);

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> electronicQueueService.join(user)
        );
        assertEquals("Призывник уже в очереди или прошёл этот этап", exception.getMessage());
        verify(summonService).findByUserId(user.getId());
        verify(summonService, never()).save(any(Summon.class));
    }

    @Test
    void summon_ShouldSetStatusToSummoned_WhenStatusIsInQueue() {
        // Arrange
        summon.setStatus(RecruitStatus.IN_QUEUE);
        when(summonService.findByUserId(anyLong())).thenReturn(summon);
        when(summonService.save(any(Summon.class))).thenReturn(summon);

        // Act
        electronicQueueService.summon(user);

        // Assert
        assertEquals(RecruitStatus.SUMMONED, summon.getStatus());
        verify(summonService).findByUserId(user.getId());
        verify(summonService).save(summon);
    }

    @Test
    void summon_ShouldThrowException_WhenStatusIsNotInQueue() {
        // Arrange
        when(summonService.findByUserId(anyLong())).thenReturn(summon);

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> electronicQueueService.summon(user)
        );
        assertEquals("Призывник не в очереди", exception.getMessage());
        verify(summonService).findByUserId(user.getId());
        verify(summonService, never()).save(any(Summon.class));
    }

    @Test
    void leave_ShouldSetStatusToNotStarted_WhenStatusIsInQueue() {
        // Arrange
        summon.setStatus(RecruitStatus.IN_QUEUE);
        when(summonService.findByUserId(anyLong())).thenReturn(summon);
        when(summonService.save(any(Summon.class))).thenReturn(summon);

        // Act
        electronicQueueService.leave(user);

        // Assert
        assertEquals(RecruitStatus.NOT_STARTED, summon.getStatus());
        verify(summonService).findByUserId(user.getId());
        verify(summonService).save(summon);
    }

    @Test
    void leave_ShouldThrowException_WhenStatusIsNotInQueue() {
        // Arrange
        when(summonService.findByUserId(anyLong())).thenReturn(summon);

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> electronicQueueService.leave(user)
        );
        assertEquals("Призывник не в очереди", exception.getMessage());
        verify(summonService).findByUserId(user.getId());
        verify(summonService, never()).save(any(Summon.class));
    }

    @Test
    void reject_ShouldSetStatusToNotStartedAndClearMilitaryBranch_WhenStatusIsSummoned() {
        // Arrange
        summon.setStatus(RecruitStatus.SUMMONED);
        summon.setMilitaryBranch("Пехота");
        when(summonService.findByUserId(anyLong())).thenReturn(summon);
        when(summonService.save(any(Summon.class))).thenReturn(summon);

        // Act
        electronicQueueService.reject(user);

        // Assert
        assertEquals(RecruitStatus.NOT_STARTED, summon.getStatus());
        assertNull(summon.getMilitaryBranch());
        verify(summonService).findByUserId(user.getId());
        verify(summonService).save(summon);
    }

    @Test
    void reject_ShouldThrowException_WhenStatusIsNotSummoned() {
        // Arrange
        summon.setStatus(RecruitStatus.IN_QUEUE);
        when(summonService.findByUserId(anyLong())).thenReturn(summon);

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> electronicQueueService.reject(user)
        );
        assertEquals("Призывник не вызван комиссаром", exception.getMessage());
        verify(summonService).findByUserId(user.getId());
        verify(summonService, never()).save(any(Summon.class));
    }

    @Test
    void isCommissarReady_ShouldReturnTrue_WhenStatusIsSummoned() {
        // Arrange
        summon.setStatus(RecruitStatus.SUMMONED);
        when(summonService.findByUsername("testUser")).thenReturn(summon);

        // Act
        boolean result = electronicQueueService.isCommissarReady("testUser");

        // Assert
        assertTrue(result);
        verify(summonService).findByUsername("testUser");
    }

    @Test
    void isCommissarReady_ShouldReturnFalse_WhenStatusIsNotSummoned() {
        // Arrange
        summon.setStatus(RecruitStatus.IN_QUEUE);
        when(summonService.findByUsername("testUser")).thenReturn(summon);

        // Act
        boolean result = electronicQueueService.isCommissarReady("testUser");

        // Assert
        assertFalse(result);
        verify(summonService).findByUsername("testUser");
    }

    @Test
    void getRecruitSummon_ShouldReturnSummon_WhenUserExists() {
        // Arrange
        when(summonService.findByUserId(user.getId())).thenReturn(summon);

        // Act
        Summon result = electronicQueueService.getRecruitSummon(user);

        // Assert
        assertNotNull(result);
        assertEquals(summon.getId(), result.getId());
        verify(summonService).findByUserId(user.getId());
    }
}
