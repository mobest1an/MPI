package com.iverpa.mpi.controller.dto.responses;

import com.iverpa.mpi.config.Passed;

import java.util.List;

@Passed
public record ConvoyResponse(
        Long convoyId,
        List<WaitingRoomResponse> recruits
) {
}
