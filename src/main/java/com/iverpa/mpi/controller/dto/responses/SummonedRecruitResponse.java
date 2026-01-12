package com.iverpa.mpi.controller.dto.responses;

import com.iverpa.mpi.config.Passed;

@Passed
public record SummonedRecruitResponse(
        Long summonId,
        String username
) {
}
