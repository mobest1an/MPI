package com.iverpa.mpi.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    @Test
    void testRoleValues() {
        // Test that all expected role values exist
        assertNotNull(Role.RECRUIT);
        assertNotNull(Role.ESCORT);
        assertNotNull(Role.COMMISSAR);
        assertNotNull(Role.ADMIN);
        assertNotNull(Role.MILITARY_POLICE);
    }

    @Test
    void testRoleNames() {
        // Test role names
        assertEquals("recruit", Role.RECRUIT.getName());
        assertEquals("escort", Role.ESCORT.getName());
        assertEquals("commissar", Role.COMMISSAR.getName());
        assertEquals("admin", Role.ADMIN.getName());
        assertEquals("military_police", Role.MILITARY_POLICE.getName());
    }

    @Test
    void testRoleToString() {
        // Test string representation
        assertEquals("RECRUIT", Role.RECRUIT.toString());
        assertEquals("ESCORT", Role.ESCORT.toString());
        assertEquals("COMMISSAR", Role.COMMISSAR.toString());
        assertEquals("ADMIN", Role.ADMIN.toString());
        assertEquals("MILITARY_POLICE", Role.MILITARY_POLICE.toString());
    }
}
