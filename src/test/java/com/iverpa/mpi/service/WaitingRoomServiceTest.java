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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WaitingRoomServiceTest {

    @Mock
    private SummonService summonService;

    @InjectMocks
    private WaitingRoomService waitingRoomService;

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
        summon.setStatus(RecruitStatus.SUMMONED);
        summon.setMilitaryBranch("Пехота");
    }

    @Test
    void sendToWaitingRoom_ShouldSetStatusToWaitingEscort_WhenStatusIsSummoned() {
        // Arrange
        String militaryBranch = "Танковые войска";
        when(summonService.findByUserId(anyLong())).thenReturn(summon);
        when(summonService.save(any(Summon.class))).thenReturn(summon);

        // Act
        waitingRoomService.sendToWaitingRoom(user, militaryBranch);

        // Assert
        assertEquals(RecruitStatus.WAITING_ESCORT, summon.getStatus());
        assertEquals(militaryBranch, summon.getMilitaryBranch());
        verify(summonService).findByUserId(user.getId());
        verify(summonService).save(summon);
    }

    @Test
    void sendToWaitingRoom_ShouldThrowException_WhenStatusIsNotSummoned() {
        // Arrange
        summon.setStatus(RecruitStatus.IN_QUEUE);
        when(summonService.findByUserId(anyLong())).thenReturn(summon);

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> waitingRoomService.sendToWaitingRoom(user, "Пехота")
        );
        assertEquals("Призывник не вызван комиссаром", exception.getMessage());
        verify(summonService).findByUserId(user.getId());
        verify(summonService, never()).save(any(Summon.class));
    }

    @Test
    void exists_ShouldReturnTrue_WhenStatusIsWaitingEscort() {
        // Arrange
        summon.setStatus(RecruitStatus.WAITING_ESCORT);
        when(summonService.findByUserId(anyLong())).thenReturn(summon);

        // Act
        boolean result = waitingRoomService.exists(user);

        // Assert
        assertTrue(result);
        verify(summonService).findByUserId(user.getId());
    }

    @Test
    void exists_ShouldReturnTrue_WhenStatusIsInConvoy() {
        // Arrange
        summon.setStatus(RecruitStatus.IN_CONVOY);
        when(summonService.findByUserId(anyLong())).thenReturn(summon);

        // Act
        boolean result = waitingRoomService.exists(user);

        // Assert
        assertTrue(result);
        verify(summonService).findByUserId(user.getId());
    }

    @Test
    void exists_ShouldReturnTrue_WhenStatusIsDone() {
        // Arrange
        summon.setStatus(RecruitStatus.DONE);
        when(summonService.findByUserId(anyLong())).thenReturn(summon);

        // Act
        boolean result = waitingRoomService.exists(user);

        // Assert
        assertTrue(result);
        verify(summonService).findByUserId(user.getId());
    }

    @Test
    void exists_ShouldReturnFalse_WhenStatusIsNotStarted() {
        // Arrange
        summon.setStatus(RecruitStatus.NOT_STARTED);
        when(summonService.findByUserId(anyLong())).thenReturn(summon);

        // Act
        boolean result = waitingRoomService.exists(user);

        // Assert
        assertFalse(result);
        verify(summonService).findByUserId(user.getId());
    }

    @Test
    void exists_ShouldReturnFalse_WhenStatusIsInQueue() {
        // Arrange
        summon.setStatus(RecruitStatus.IN_QUEUE);
        when(summonService.findByUserId(anyLong())).thenReturn(summon);

        // Act
        boolean result = waitingRoomService.exists(user);

        // Assert
        assertFalse(result);
        verify(summonService).findByUserId(user.getId());
    }

    @Test
    void exists_ShouldReturnFalse_WhenStatusIsSummoned() {
        // Arrange
        summon.setStatus(RecruitStatus.SUMMONED);
        when(summonService.findByUserId(anyLong())).thenReturn(summon);

        // Act
        boolean result = waitingRoomService.exists(user);

        // Assert
        assertFalse(result);
        verify(summonService).findByUserId(user.getId());
    }

    @Test
    void getWaitingRoom_ShouldReturnListOfWaitingRoomResponses() {
        // Arrange
        Summon summon1 = new Summon();
        summon1.setId(1L);
        User user1 = new User();
        user1.setUsername("user1");
        summon1.setUser(user1);
        summon1.setMilitaryBranch("Пехота");

        Summon summon2 = new Summon();
        summon2.setId(2L);
        User user2 = new User();
        user2.setUsername("user2");
        summon2.setUser(user2);
        summon2.setMilitaryBranch("Танковые войска");

        when(summonService.findAllByStatus(RecruitStatus.WAITING_ESCORT)).thenReturn(List.of(summon1, summon2));

        // Act
        var result = waitingRoomService.getWaitingRoom();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("user1", result.get(0).username());
        assertEquals("Пехота", result.get(0).militaryBranch());
        assertEquals("user2", result.get(1).username());
        assertEquals("Танковые войска", result.get(1).militaryBranch());
        verify(summonService).findAllByStatus(RecruitStatus.WAITING_ESCORT);
    }

    @Test
    void getWaitingRoom_ShouldReturnEmptyList_WhenNoSummonsWithWaitingEscortStatus() {
        // Arrange
        when(summonService.findAllByStatus(RecruitStatus.WAITING_ESCORT)).thenReturn(List.of());

        // Act
        var result = waitingRoomService.getWaitingRoom();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(summonService).findAllByStatus(RecruitStatus.WAITING_ESCORT);
    }
}
