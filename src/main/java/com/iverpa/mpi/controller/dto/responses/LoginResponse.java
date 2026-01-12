package com.iverpa.mpi.controller.dto.responses;

import com.iverpa.mpi.config.Passed;

import java.util.Set;

@Passed
public record LoginResponse(String token, Set<String> roles) {

}
