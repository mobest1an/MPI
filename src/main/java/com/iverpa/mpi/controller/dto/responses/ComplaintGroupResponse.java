package com.iverpa.mpi.controller.dto.responses;

import com.iverpa.mpi.config.Passed;

@Passed
public record ComplaintGroupResponse(
        Long convoyId,
        String escortUsername,
        int complaintsCount,
        boolean takenByOther
) {
}
