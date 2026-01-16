package com.iverpa.mpi.controller.dto.responses;

import java.util.Set;

public record ExtendedLoginResponse(String token, Set<String> roles, String redirectUrl) {
}
