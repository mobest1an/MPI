package com.iverpa.mpi.controller;

import com.iverpa.mpi.controller.dto.requests.LoginRequest;
import com.iverpa.mpi.controller.dto.requests.RegisterRequest;
import com.iverpa.mpi.controller.dto.responses.LoginResponse;
import com.iverpa.mpi.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) throws Exception {
        return authService.login(request);
    }

    @PostMapping("/register")
    public void register(@RequestBody RegisterRequest request) {
        authService.register(request);
    }
}
