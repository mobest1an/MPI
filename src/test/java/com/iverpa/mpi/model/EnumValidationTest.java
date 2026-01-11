package com.iverpa.mpi.model;

/**
 * Simple validation test for enums that doesn't require external dependencies
 */
public class EnumValidationTest {
    
    public static void main(String[] args) {
        validateRecruitStatus();
        validateComplaintStatus();
        validateRole();
        System.out.println("All enum validations passed!");
    }
    
    private static void validateRecruitStatus() {
        // Validate that all RecruitStatus enum values exist and are accessible
        try {
            RecruitStatus[] values = RecruitStatus.values();
            if (values.length != 6) {
                throw new RuntimeException("Expected 6 RecruitStatus values, got " + values.length);
            }
            
            // Check specific values exist
            RecruitStatus notStarted = RecruitStatus.valueOf("NOT_STARTED");
            if (notStarted == null) {
                throw new RuntimeException("NOT_STARTED value not found");
            }
            
            RecruitStatus inQueue = RecruitStatus.valueOf("IN_QUEUE");
            if (inQueue == null) {
                throw new RuntimeException("IN_QUEUE value not found");
            }
            
            RecruitStatus summoned = RecruitStatus.valueOf("SUMMONED");
            if (summoned == null) {
                throw new RuntimeException("SUMMONED value not found");
            }
            
            RecruitStatus waitingEscort = RecruitStatus.valueOf("WAITING_ESCORT");
            if (waitingEscort == null) {
                throw new RuntimeException("WAITING_ESCORT value not found");
            }
            
            RecruitStatus inConvoy = RecruitStatus.valueOf("IN_CONVOY");
            if (inConvoy == null) {
                throw new RuntimeException("IN_CONVOY value not found");
            }
            
            RecruitStatus done = RecruitStatus.valueOf("DONE");
            if (done == null) {
                throw new RuntimeException("DONE value not found");
            }
            
            System.out.println("RecruitStatus validation passed");
        } catch (Exception e) {
            throw new RuntimeException("RecruitStatus validation failed: " + e.getMessage(), e);
        }
    }
    
    private static void validateComplaintStatus() {
        // Validate that all ComplaintStatus enum values exist and are accessible
        try {
            ComplaintStatus[] values = ComplaintStatus.values();
            if (values.length != 3) {
                throw new RuntimeException("Expected 3 ComplaintStatus values, got " + values.length);
            }
            
            // Check specific values exist
            ComplaintStatus newStatus = ComplaintStatus.valueOf("NEW");
            if (newStatus == null) {
                throw new RuntimeException("NEW value not found");
            }
            
            ComplaintStatus inProgress = ComplaintStatus.valueOf("IN_PROGRESS");
            if (inProgress == null) {
                throw new RuntimeException("IN_PROGRESS value not found");
            }
            
            ComplaintStatus completed = ComplaintStatus.valueOf("COMPLETED");
            if (completed == null) {
                throw new RuntimeException("COMPLETED value not found");
            }
            
            System.out.println("ComplaintStatus validation passed");
        } catch (Exception e) {
            throw new RuntimeException("ComplaintStatus validation failed: " + e.getMessage(), e);
        }
    }
    
    private static void validateRole() {
        // Validate that all Role enum values exist and are accessible
        try {
            Role[] values = Role.values();
            if (values.length != 5) {
                throw new RuntimeException("Expected 5 Role values, got " + values.length);
            }
            
            // Check specific values exist
            Role recruit = Role.valueOf("RECRUIT");
            if (recruit == null) {
                throw new RuntimeException("RECRUIT value not found");
            }
            
            Role escort = Role.valueOf("ESCORT");
            if (escort == null) {
                throw new RuntimeException("ESCORT value not found");
            }
            
            Role commissar = Role.valueOf("COMMISSAR");
            if (commissar == null) {
                throw new RuntimeException("COMMISSAR value not found");
            }
            
            Role admin = Role.valueOf("ADMIN");
            if (admin == null) {
                throw new RuntimeException("ADMIN value not found");
            }
            
            Role militaryPolice = Role.valueOf("MILITARY_POLICE");
            if (militaryPolice == null) {
                throw new RuntimeException("MILITARY_POLICE value not found");
            }
            
            System.out.println("Role validation passed");
        } catch (Exception e) {
            throw new RuntimeException("Role validation failed: " + e.getMessage(), e);
        }
    }
}