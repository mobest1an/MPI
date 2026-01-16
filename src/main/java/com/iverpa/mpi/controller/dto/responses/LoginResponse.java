package com.iverpa.mpi.controller.dto.responses;

import java.util.Set;

public record LoginResponse(String token, Set<String> roles) {

}
