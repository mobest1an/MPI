package com.iverpa.mpi.controller.dto.responses;

public record ComplaintGroupResponse(
        Long convoyId,
        String escortUsername,
        int complaintsCount,
        boolean takenByOther
) {
}
