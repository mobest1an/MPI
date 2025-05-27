package com.iverpa.mpi.service;

import com.iverpa.mpi.dto.requests.LoginRequest;
import com.iverpa.mpi.dto.responses.LoginResponse;
import com.iverpa.mpi.security.JwtUtils;
import com.iverpa.mpi.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtils jwtUtils;

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

        UserDetailsImpl user = (UserDetailsImpl) userDetailsService.loadUserByUsername(request.username());
        String token = jwtUtils.generateToken(user.getUsername(), user.getRoles());
        return new LoginResponse(token);
    }
}
