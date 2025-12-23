package com.iverpa.mpi.controller.dto.requests;

public record SendToWaitingRoomRequest(
        String username,
        String militaryBranch
) {
}
