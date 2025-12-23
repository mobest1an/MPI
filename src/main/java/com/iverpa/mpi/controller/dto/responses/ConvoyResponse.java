package com.iverpa.mpi.controller.dto.responses;

import java.util.List;

public record ConvoyResponse(
        Long convoyId,
        List<WaitingRoomResponse> recruits
) {
}
