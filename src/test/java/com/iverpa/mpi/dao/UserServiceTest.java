package com.iverpa.mpi.dao;

import com.iverpa.mpi.dao.repository.UserRepository;
import com.iverpa.mpi.model.Role;
import com.iverpa.mpi.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setPassword("encodedPassword");
        user.setRoles(new HashSet<>(Set.of(Role.RECRUIT)));
    }

    @Test
    void findByUsername_ShouldReturnUser_WhenUserExists() {
        // Arrange
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));

        // Act
        User result = userService.findByUsername("testUser");

        // Assert
        assertNotNull(result);
        assertEquals("testUser", result.getUsername());
        assertEquals(1L, result.getId());
        verify(userRepository).findByUsername("testUser");
    }

    @Test
    void findByUsername_ShouldThrowException_WhenUserDoesNotExist() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userService.findByUsername("nonexistent")
        );
        assertEquals("User not found: nonexistent", exception.getMessage());
        verify(userRepository).findByUsername("nonexistent");
    }

    @Test
    void existsByUsername_ShouldReturnTrue_WhenUserExists() {
        // Arrange
        when(userRepository.existsByUsername("testUser")).thenReturn(true);

        // Act
        boolean result = userService.existsByUsername("testUser");

        // Assert
        assertTrue(result);
        verify(userRepository).existsByUsername("testUser");
    }

    @Test
    void existsByUsername_ShouldReturnFalse_WhenUserDoesNotExist() {
        // Arrange
        when(userRepository.existsByUsername("nonexistent")).thenReturn(false);

        // Act
        boolean result = userService.existsByUsername("nonexistent");

        // Assert
        assertFalse(result);
        verify(userRepository).existsByUsername("nonexistent");
    }

    @Test
    void addRole_ShouldAddRoleToUser_WhenUserExists() {
        // Arrange
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userService.addRole("testUser", Role.COMMISSAR);

        // Assert
        assertNotNull(result);
        assertTrue(result.getRoles().contains(Role.COMMISSAR));
        assertTrue(result.getRoles().contains(Role.RECRUIT));
        assertEquals(2, result.getRoles().size());
        verify(userRepository).findByUsername("testUser");
        verify(userRepository).save(user);
    }

    @Test
    void addRole_ShouldThrowException_WhenUserDoesNotExist() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                UsernameNotFoundException.class,
                () -> userService.addRole("nonexistent", Role.ADMIN)
        );
        verify(userRepository).findByUsername("nonexistent");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void addRole_ShouldNotDuplicateRole_WhenRoleAlreadyExists() {
        // Arrange
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act - добавляем роль, которая уже есть
        User result = userService.addRole("testUser", Role.RECRUIT);

        // Assert - Set не позволяет дубликаты
        assertEquals(1, result.getRoles().size());
        assertTrue(result.getRoles().contains(Role.RECRUIT));
        verify(userRepository).save(user);
    }

    @Test
    void deleteRole_ShouldRemoveRoleFromUser_WhenUserExists() {
        // Arrange
        user.getRoles().add(Role.COMMISSAR); // Добавляем вторую роль
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userService.deleteRole("testUser", Role.COMMISSAR);

        // Assert
        assertNotNull(result);
        assertFalse(result.getRoles().contains(Role.COMMISSAR));
        assertTrue(result.getRoles().contains(Role.RECRUIT));
        assertEquals(1, result.getRoles().size());
        verify(userRepository).findByUsername("testUser");
        verify(userRepository).save(user);
    }

    @Test
    void deleteRole_ShouldThrowException_WhenUserDoesNotExist() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                UsernameNotFoundException.class,
                () -> userService.deleteRole("nonexistent", Role.RECRUIT)
        );
        verify(userRepository).findByUsername("nonexistent");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteRole_ShouldDoNothing_WhenRoleDoesNotExist() {
        // Arrange
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act - удаляем роль, которой нет
        User result = userService.deleteRole("testUser", Role.ADMIN);

        // Assert - ничего не изменилось
        assertEquals(1, result.getRoles().size());
        assertTrue(result.getRoles().contains(Role.RECRUIT));
        verify(userRepository).save(user);
    }

    @Test
    void save_ShouldSaveAndReturnUser() {
        // Arrange
        User newUser = new User();
        newUser.setUsername("newUser");
        newUser.setPassword("password");
        newUser.setRoles(Set.of(Role.RECRUIT));

        when(userRepository.save(newUser)).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        // Act
        User result = userService.save(newUser);

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("newUser", result.getUsername());
        verify(userRepository).save(newUser);
    }

    @Test
    void save_ShouldUpdateExistingUser() {
        // Arrange
        user.setPassword("newPassword");
        when(userRepository.save(user)).thenReturn(user);

        // Act
        User result = userService.save(user);

        // Assert
        assertNotNull(result);
        assertEquals("newPassword", result.getPassword());
        verify(userRepository).save(user);
    }
}
