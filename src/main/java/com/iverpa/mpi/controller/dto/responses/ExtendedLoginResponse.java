package com.iverpa.mpi.controller.dto.responses;

import com.iverpa.mpi.config.Passed;

import java.util.Set;

@Passed
public record ExtendedLoginResponse(String token, Set<String> roles, String redirectUrl) {
}
