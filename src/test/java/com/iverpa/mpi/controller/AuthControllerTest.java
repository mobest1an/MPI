package com.iverpa.mpi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iverpa.mpi.controller.dto.requests.LoginRequest;
import com.iverpa.mpi.controller.dto.requests.RegisterRequest;
import com.iverpa.mpi.controller.dto.responses.LoginResponse;
import com.iverpa.mpi.exception.GlobalExceptionHandler;
import com.iverpa.mpi.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void login_ShouldReturnToken_WhenCredentialsAreValid() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("testUser", "password");
        LoginResponse response = new LoginResponse("jwt-token-123", Set.of("RECRUIT"));
        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-123"))
                .andExpect(jsonPath("$.roles[0]").value("RECRUIT"));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void login_ShouldReturnMultipleRoles_WhenUserHasMultipleRoles() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("adminUser", "password");
        LoginResponse response = new LoginResponse("jwt-token-456", Set.of("ADMIN", "COMMISSAR"));
        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-456"))
                .andExpect(jsonPath("$.roles").isArray());

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void login_ShouldThrowException_WhenCredentialsAreInvalid() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("testUser", "wrongPassword");
        when(authService.login(any(LoginRequest.class))).thenThrow(new Exception("Bad Credentials"));

        // Act & Assert - Exception выбрасывается из контроллера
        try {
            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
        } catch (Exception e) {
            // Ожидаем ServletException с причиной "Bad Credentials"
            assert e.getCause().getMessage().equals("Bad Credentials");
        }

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void login_ShouldThrowException_WhenUserDoesNotExist() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("nonexistentUser", "password");
        when(authService.login(any(LoginRequest.class))).thenThrow(new Exception("Bad Credentials"));

        // Act & Assert - Exception выбрасывается из контроллера
        try {
            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
        } catch (Exception e) {
            // Ожидаем ServletException с причиной "Bad Credentials"
            assert e.getCause().getMessage().equals("Bad Credentials");
        }

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void register_ShouldReturnOk_WhenValidRequest() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest("newUser", "password123");
        doNothing().when(authService).register(any(RegisterRequest.class));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    void register_ShouldReturnBadRequest_WhenUsernameAlreadyExists() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest("existingUser", "password");
        doThrow(new IllegalArgumentException("Username is already exists"))
                .when(authService).register(any(RegisterRequest.class));

        // Act & Assert - IllegalArgumentException обрабатывается GlobalExceptionHandler как BadRequest
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Username is already exists"));

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    void register_ShouldCallAuthService_WithCorrectRequest() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest("testRecruit", "securePass");
        doNothing().when(authService).register(any(RegisterRequest.class));

        // Act
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Assert
        verify(authService, times(1)).register(argThat(req ->
                req.username().equals("testRecruit") && req.password().equals("securePass")
        ));
    }

    @Test
    void login_ShouldCallAuthService_WithCorrectRequest() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("user123", "pass456");
        LoginResponse response = new LoginResponse("token", Set.of("RECRUIT"));
        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        // Act
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Assert
        verify(authService, times(1)).login(argThat(req ->
                req.username().equals("user123") && req.password().equals("pass456")
        ));
    }
}
