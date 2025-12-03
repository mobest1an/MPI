package com.iverpa.mpi.model;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class UserDetailsImpl implements UserDetails {

    private final String username;
    private final String password;
    @Getter
    private final Set<Role> roles;

    public UserDetailsImpl(String username, String password, Set<Role> roles) {
        this.username = username;
        this.password = password;
        this.roles = roles;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream().map(role -> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public User toEntity(Long id) {
        if (id != null) {
            return new User(
                    id,
                    username,
                    password,
                    roles,
                    null
            );
        } else {
            return new User(
                    null,
                    username,
                    password,
                    roles,
                    null
            );
        }
    }
}
