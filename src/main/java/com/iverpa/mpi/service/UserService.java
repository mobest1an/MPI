package com.iverpa.mpi.service;

import com.iverpa.mpi.model.Role;
import com.iverpa.mpi.model.User;
import com.iverpa.mpi.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void initAdminUser() {
        if (!userRepository.existsByUsername("admin")) {
            var admin = new User(
                    0L,
                    "admin",
                    passwordEncoder.encode("admin"),
                    Set.of(Role.ADMIN)
            );
            userRepository.save(admin);
        }
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException("User not found: " + username)
        );
    }

    public User addRole(String username, Role role) {
        User user = findByUsername(username);
        user.getRoles().add(role);
        return save(user);
    }

    public User deleteRole(String username, Role role) {
        User user = findByUsername(username);
        user.getRoles().remove(role);
        return save(user);
    }

    public User save(User user) {
        return userRepository.save(user);
    }
}
