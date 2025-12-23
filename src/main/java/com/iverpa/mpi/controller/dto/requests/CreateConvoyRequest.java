package com.iverpa.mpi.controller.dto.requests;

import java.util.List;

public record CreateConvoyRequest(
        List<Long> summonIds
) {
}
