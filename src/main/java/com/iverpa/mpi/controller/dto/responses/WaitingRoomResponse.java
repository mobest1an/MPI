package com.iverpa.mpi.controller.dto.responses;

public record WaitingRoomResponse(
        Long summonId,
        String username,
        String militaryBranch
) {
}
