package com.iverpa.mpi.controller.dto.requests;

import com.iverpa.mpi.config.Passed;

@Passed
public record SendToWaitingRoomRequest(
        String username,
        String militaryBranch
) {
}
