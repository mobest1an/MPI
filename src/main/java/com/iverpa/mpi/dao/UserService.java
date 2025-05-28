package com.iverpa.mpi.dao;

import com.iverpa.mpi.model.Role;
import com.iverpa.mpi.model.User;
import com.iverpa.mpi.dao.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException("User not found: " + username)
        );
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
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
