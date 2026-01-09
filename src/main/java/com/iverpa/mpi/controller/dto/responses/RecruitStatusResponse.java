package com.iverpa.mpi.controller.dto.responses;

import com.iverpa.mpi.model.RecruitStatus;

public record RecruitStatusResponse(
        RecruitStatus status,
        String militaryBranch
) {
}
