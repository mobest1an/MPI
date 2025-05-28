package com.iverpa.mpi.service;

import com.iverpa.mpi.dao.UserDetailsServiceImpl;
import com.iverpa.mpi.controller.dto.requests.LoginRequest;
import com.iverpa.mpi.controller.dto.requests.RegisterRequest;
import com.iverpa.mpi.controller.dto.responses.LoginResponse;
import com.iverpa.mpi.model.Role;
import com.iverpa.mpi.model.UserDetailsImpl;
import com.iverpa.mpi.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) throws Exception {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username(),
                            request.password()
                    )
            );
        } catch (Exception e) {
            throw new Exception("Bad Credentials");
        }

        UserDetailsImpl user = userDetailsService.loadUserByUsername(request.username());
        String token = jwtUtils.generateToken(user.getUsername(), user.getRoles());
        return new LoginResponse(token);
    }

    public void register(RegisterRequest request) {
        userDetailsService.save(
                new UserDetailsImpl(
                        request.username(),
                        passwordEncoder.encode(request.password()),
                        Set.of(Role.RECRUIT)
                )
        );
    }
}
