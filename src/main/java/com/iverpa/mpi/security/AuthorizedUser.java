package com.iverpa.mpi.security;

import com.iverpa.mpi.model.Role;

import java.util.Set;

public record AuthorizedUser(String username, Set<Role> roles) {
}
