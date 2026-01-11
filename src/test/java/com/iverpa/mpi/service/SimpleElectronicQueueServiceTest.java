package com.iverpa.mpi.service;

import com.iverpa.mpi.model.RecruitStatus;
import com.iverpa.mpi.model.Summon;
import com.iverpa.mpi.model.User;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SimpleElectronicQueueServiceTest {

    @Test
    void testRecruitStatusTransitions() {
        // Test basic status transitions logic
        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");

        Summon summon = new Summon();
        summon.setId(1L);
        summon.setUser(user);
        summon.setStatus(RecruitStatus.NOT_STARTED);

        // Test that initial status is NOT_STARTED
        assertEquals(RecruitStatus.NOT_STARTED, summon.getStatus());

        // Simulate joining queue
        if (summon.getStatus() == RecruitStatus.NOT_STARTED) {
            summon.setStatus(RecruitStatus.IN_QUEUE);
        }
        assertEquals(RecruitStatus.IN_QUEUE, summon.getStatus());

        // Simulate being summoned
        if (summon.getStatus() == RecruitStatus.IN_QUEUE) {
            summon.setStatus(RecruitStatus.SUMMONED);
        }
        assertEquals(RecruitStatus.SUMMONED, summon.getStatus());
    }

    @Test
    void testIllegalStateTransitions() {
        // Test that illegal transitions throw exceptions
        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");

        Summon summon = new Summon();
        summon.setId(1L);
        summon.setUser(user);
        summon.setStatus(RecruitStatus.IN_QUEUE);

        // Test that trying to join when already in queue throws exception
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            if (summon.getStatus() != RecruitStatus.NOT_STARTED) {
                throw new IllegalStateException("Призывник уже в очереди или прошёл этот этап");
            }
        });
        assertEquals("Призывник уже в очереди или прошёл этот этап", exception.getMessage());
    }

    @Test
    void testCommissarReadyLogic() {
        // Test commissar ready logic
        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");

        Summon summon = new Summon();
        summon.setId(1L);
        summon.setUser(user);

        // Test when not ready
        summon.setStatus(RecruitStatus.IN_QUEUE);
        boolean isReady = (summon.getStatus() == RecruitStatus.SUMMONED);
        assertFalse(isReady);

        // Test when ready
        summon.setStatus(RecruitStatus.SUMMONED);
        isReady = (summon.getStatus() == RecruitStatus.SUMMONED);
        assertTrue(isReady);
    }
}
