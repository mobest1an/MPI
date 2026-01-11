package com.iverpa.mpi.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RecruitStatusTest {

    @Test
    void testRecruitStatusValues() {
        // Test that all expected status values exist
        assertNotNull(RecruitStatus.NOT_STARTED);
        assertNotNull(RecruitStatus.IN_QUEUE);
        assertNotNull(RecruitStatus.SUMMONED);
        assertNotNull(RecruitStatus.WAITING_ESCORT);
        assertNotNull(RecruitStatus.IN_CONVOY);
        assertNotNull(RecruitStatus.DONE);
    }

    @Test
    void testRecruitStatusOrdinal() {
        // Test the order of statuses
        assertEquals(0, RecruitStatus.NOT_STARTED.ordinal());
        assertEquals(1, RecruitStatus.IN_QUEUE.ordinal());
        assertEquals(2, RecruitStatus.SUMMONED.ordinal());
        assertEquals(3, RecruitStatus.WAITING_ESCORT.ordinal());
        assertEquals(4, RecruitStatus.IN_CONVOY.ordinal());
        assertEquals(5, RecruitStatus.DONE.ordinal());
    }

    @Test
    void testRecruitStatusToString() {
        // Test string representation
        assertEquals("NOT_STARTED", RecruitStatus.NOT_STARTED.toString());
        assertEquals("IN_QUEUE", RecruitStatus.IN_QUEUE.toString());
        assertEquals("SUMMONED", RecruitStatus.SUMMONED.toString());
        assertEquals("WAITING_ESCORT", RecruitStatus.WAITING_ESCORT.toString());
        assertEquals("IN_CONVOY", RecruitStatus.IN_CONVOY.toString());
        assertEquals("DONE", RecruitStatus.DONE.toString());
    }
}
