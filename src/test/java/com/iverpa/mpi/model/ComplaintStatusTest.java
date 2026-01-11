package com.iverpa.mpi.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ComplaintStatusTest {

    @Test
    void testComplaintStatusValues() {
        // Test that all expected status values exist
        assertNotNull(ComplaintStatus.NEW);
        assertNotNull(ComplaintStatus.IN_PROGRESS);
        assertNotNull(ComplaintStatus.COMPLETED);
    }

    @Test
    void testComplaintStatusOrdinal() {
        // Test the order of statuses
        assertEquals(0, ComplaintStatus.NEW.ordinal());
        assertEquals(1, ComplaintStatus.IN_PROGRESS.ordinal());
        assertEquals(2, ComplaintStatus.COMPLETED.ordinal());
    }

    @Test
    void testComplaintStatusToString() {
        // Test string representation
        assertEquals("NEW", ComplaintStatus.NEW.toString());
        assertEquals("IN_PROGRESS", ComplaintStatus.IN_PROGRESS.toString());
        assertEquals("COMPLETED", ComplaintStatus.COMPLETED.toString());
    }
}
