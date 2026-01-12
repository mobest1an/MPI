package com.iverpa.mpi.controller.dto.responses;

import com.iverpa.mpi.config.Passed;

import java.util.List;

@Passed
public record ActiveComplaintResponse(
        Long convoyId,
        String escortUsername,
        List<String> recruitUsernames,
        int complaintsCount
) {
}
