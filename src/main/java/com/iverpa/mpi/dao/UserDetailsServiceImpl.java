package com.iverpa.mpi.dao;

import com.iverpa.mpi.model.UserDetailsImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

@Repository
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;

    public UserDetailsServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetailsImpl loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userService.findByUsername(username);
        return new UserDetailsImpl(user.getUsername(), user.getPassword(), user.getRoles());
    }

    public void save(UserDetailsImpl userDetails) {
        var user = userDetails.toEntity(null);
        if (userService.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username is already exists");
        } else {
            userService.save(user);
        }
    }
}
