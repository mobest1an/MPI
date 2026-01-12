package com.iverpa.mpi.controller.dto.requests;

import com.iverpa.mpi.config.Passed;

import java.util.List;

@Passed
public record CreateConvoyRequest(
        List<Long> summonIds
) {
}
