package com.iverpa.mpi.service;

import com.iverpa.mpi.controller.dto.requests.LoginRequest;
import com.iverpa.mpi.controller.dto.requests.RegisterRequest;
import com.iverpa.mpi.controller.dto.responses.LoginResponse;
import com.iverpa.mpi.dao.UserDetailsServiceImpl;
import com.iverpa.mpi.dao.repository.SummonRepository;
import com.iverpa.mpi.model.RecruitStatus;
import com.iverpa.mpi.model.Role;
import com.iverpa.mpi.model.Summon;
import com.iverpa.mpi.model.User;
import com.iverpa.mpi.model.UserDetailsImpl;
import com.iverpa.mpi.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SummonRepository summonRepository;

    @InjectMocks
    private AuthService authService;

    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        userDetails = new UserDetailsImpl(
                "testUser",
                "encodedPassword",
                Set.of(Role.RECRUIT)
        );
    }

    @Test
    void login_ShouldReturnLoginResponse_WhenCredentialsAreValid() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("testUser", "password");
        String expectedToken = "jwt-token";

        when(userDetailsService.loadUserByUsername("testUser")).thenReturn(userDetails);
        when(jwtUtils.generateToken("testUser", Set.of(Role.RECRUIT))).thenReturn(expectedToken);

        // Act
        LoginResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals(expectedToken, response.token());
        assertTrue(response.roles().contains("RECRUIT"));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService).loadUserByUsername("testUser");
        verify(jwtUtils).generateToken("testUser", Set.of(Role.RECRUIT));
    }

    @Test
    void login_ShouldThrowException_WhenCredentialsAreInvalid() {
        // Arrange
        LoginRequest request = new LoginRequest("testUser", "wrongPassword");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        Exception exception = assertThrows(
                Exception.class,
                () -> authService.login(request)
        );
        assertEquals("Bad Credentials", exception.getMessage());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService, never()).loadUserByUsername(anyString());
    }

    @Test
    void login_ShouldReturnMultipleRoles_WhenUserHasMultipleRoles() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("adminUser", "password");
        String expectedToken = "jwt-token";
        UserDetailsImpl adminUser = new UserDetailsImpl(
                "adminUser",
                "encodedPassword",
                Set.of(Role.ADMIN, Role.COMMISSAR)
        );

        when(userDetailsService.loadUserByUsername("adminUser")).thenReturn(adminUser);
        when(jwtUtils.generateToken("adminUser", Set.of(Role.ADMIN, Role.COMMISSAR))).thenReturn(expectedToken);

        // Act
        LoginResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals(expectedToken, response.token());
        assertEquals(2, response.roles().size());
        assertTrue(response.roles().contains("ADMIN"));
        assertTrue(response.roles().contains("COMMISSAR"));
    }

    @Test
    void register_ShouldCreateUserAndSummon_WhenValidRequest() {
        // Arrange
        RegisterRequest request = new RegisterRequest("newUser", "password123");
        String encodedPassword = "encodedPassword123";
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("newUser");

        when(passwordEncoder.encode("password123")).thenReturn(encodedPassword);
        when(userDetailsService.save(any(UserDetailsImpl.class))).thenReturn(savedUser);
        when(summonRepository.save(any(Summon.class))).thenAnswer(invocation -> {
            Summon summon = invocation.getArgument(0);
            summon.setId(1L);
            return summon;
        });

        // Act
        authService.register(request);

        // Assert
        verify(passwordEncoder).encode("password123");

        // Проверяем, что UserDetailsImpl был создан с правильными параметрами
        ArgumentCaptor<UserDetailsImpl> userCaptor = ArgumentCaptor.forClass(UserDetailsImpl.class);
        verify(userDetailsService).save(userCaptor.capture());
        UserDetailsImpl capturedUser = userCaptor.getValue();
        assertEquals("newUser", capturedUser.getUsername());
        assertEquals(encodedPassword, capturedUser.getPassword());
        assertTrue(capturedUser.getRoles().contains(Role.RECRUIT));

        // Проверяем, что Summon был создан с правильными параметрами
        ArgumentCaptor<Summon> summonCaptor = ArgumentCaptor.forClass(Summon.class);
        verify(summonRepository).save(summonCaptor.capture());
        Summon capturedSummon = summonCaptor.getValue();
        assertEquals(savedUser, capturedSummon.getUser());
        assertEquals(RecruitStatus.NOT_STARTED, capturedSummon.getStatus());
    }

    @Test
    void register_ShouldAssignRecruitRole_ToNewUser() {
        // Arrange
        RegisterRequest request = new RegisterRequest("recruit", "pass");
        User savedUser = new User();
        savedUser.setId(1L);

        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userDetailsService.save(any(UserDetailsImpl.class))).thenReturn(savedUser);

        // Act
        authService.register(request);

        // Assert
        ArgumentCaptor<UserDetailsImpl> userCaptor = ArgumentCaptor.forClass(UserDetailsImpl.class);
        verify(userDetailsService).save(userCaptor.capture());
        assertEquals(Set.of(Role.RECRUIT), userCaptor.getValue().getRoles());
    }

    @Test
    void register_ShouldSetSummonStatusToNotStarted() {
        // Arrange
        RegisterRequest request = new RegisterRequest("newRecruit", "password");
        User savedUser = new User();
        savedUser.setId(1L);

        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userDetailsService.save(any(UserDetailsImpl.class))).thenReturn(savedUser);

        // Act
        authService.register(request);

        // Assert
        ArgumentCaptor<Summon> summonCaptor = ArgumentCaptor.forClass(Summon.class);
        verify(summonRepository).save(summonCaptor.capture());
        assertEquals(RecruitStatus.NOT_STARTED, summonCaptor.getValue().getStatus());
    }
}
