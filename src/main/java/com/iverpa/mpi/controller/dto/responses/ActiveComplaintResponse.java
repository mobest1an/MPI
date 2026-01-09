package com.iverpa.mpi.controller.dto.responses;

import java.util.List;

public record ActiveComplaintResponse(
        Long convoyId,
        String escortUsername,
        List<String> recruitUsernames,
        int complaintsCount
) {
}
