package com.iverpa.mpi.dao;

import com.iverpa.mpi.model.Role;
import com.iverpa.mpi.model.User;
import com.iverpa.mpi.model.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setPassword("encodedPassword");
        user.setRoles(Set.of(Role.RECRUIT));
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserExists() {
        // Arrange
        when(userService.findByUsername("testUser")).thenReturn(user);

        // Act
        UserDetailsImpl result = userDetailsService.loadUserByUsername("testUser");

        // Assert
        assertNotNull(result);
        assertEquals("testUser", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());
        assertTrue(result.getRoles().contains(Role.RECRUIT));
        verify(userService).findByUsername("testUser");
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenUserDoesNotExist() {
        // Arrange
        when(userService.findByUsername("nonexistent"))
                .thenThrow(new UsernameNotFoundException("User not found: nonexistent"));

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("nonexistent")
        );
        assertEquals("User not found: nonexistent", exception.getMessage());
        verify(userService).findByUsername("nonexistent");
    }

    @Test
    void loadUserByUsername_ShouldReturnCorrectRoles_WhenUserHasMultipleRoles() {
        // Arrange
        user.setRoles(Set.of(Role.ADMIN, Role.COMMISSAR, Role.ESCORT));
        when(userService.findByUsername("adminUser")).thenReturn(user);

        // Act
        UserDetailsImpl result = userDetailsService.loadUserByUsername("adminUser");

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getRoles().size());
        assertTrue(result.getRoles().contains(Role.ADMIN));
        assertTrue(result.getRoles().contains(Role.COMMISSAR));
        assertTrue(result.getRoles().contains(Role.ESCORT));
    }

    @Test
    void save_ShouldSaveUser_WhenUsernameDoesNotExist() {
        // Arrange
        UserDetailsImpl userDetails = new UserDetailsImpl("newUser", "password", Set.of(Role.RECRUIT));
        User savedUser = new User(1L, "newUser", "password", Set.of(Role.RECRUIT));

        when(userService.existsByUsername("newUser")).thenReturn(false);
        when(userService.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = userDetailsService.save(userDetails);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("newUser", result.getUsername());
        verify(userService).existsByUsername("newUser");
        verify(userService).save(any(User.class));
    }

    @Test
    void save_ShouldThrowException_WhenUsernameAlreadyExists() {
        // Arrange
        UserDetailsImpl userDetails = new UserDetailsImpl("existingUser", "password", Set.of(Role.RECRUIT));
        when(userService.existsByUsername("existingUser")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userDetailsService.save(userDetails)
        );
        assertEquals("Username is already exists", exception.getMessage());
        verify(userService).existsByUsername("existingUser");
        verify(userService, never()).save(any(User.class));
    }

    @Test
    void save_ShouldPassCorrectUserToUserService() {
        // Arrange
        UserDetailsImpl userDetails = new UserDetailsImpl("testRecruit", "securePass", Set.of(Role.RECRUIT));
        when(userService.existsByUsername("testRecruit")).thenReturn(false);
        when(userService.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        // Act
        User result = userDetailsService.save(userDetails);

        // Assert
        verify(userService).save(argThat(u ->
                u.getUsername().equals("testRecruit") &&
                u.getPassword().equals("securePass") &&
                u.getRoles().contains(Role.RECRUIT)
        ));
    }

    @Test
    void save_ShouldCreateUserWithNullId() {
        // Arrange
        UserDetailsImpl userDetails = new UserDetailsImpl("newUser", "password", Set.of(Role.RECRUIT));
        when(userService.existsByUsername("newUser")).thenReturn(false);
        when(userService.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        userDetailsService.save(userDetails);

        // Assert - проверяем, что ID null (будет сгенерирован базой)
        verify(userService).save(argThat(u -> u.getId() == null));
    }

    @Test
    void save_ShouldPreserveAllRoles() {
        // Arrange
        Set<Role> roles = Set.of(Role.ADMIN, Role.COMMISSAR);
        UserDetailsImpl userDetails = new UserDetailsImpl("admin", "password", roles);
        when(userService.existsByUsername("admin")).thenReturn(false);
        when(userService.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userDetailsService.save(userDetails);

        // Assert
        verify(userService).save(argThat(u ->
                u.getRoles().size() == 2 &&
                u.getRoles().contains(Role.ADMIN) &&
                u.getRoles().contains(Role.COMMISSAR)
        ));
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetailsWithAuthorities() {
        // Arrange
        when(userService.findByUsername("testUser")).thenReturn(user);

        // Act
        UserDetailsImpl result = userDetailsService.loadUserByUsername("testUser");

        // Assert
        assertNotNull(result.getAuthorities());
        assertFalse(result.getAuthorities().isEmpty());
    }
}
