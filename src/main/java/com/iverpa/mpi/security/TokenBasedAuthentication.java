package com.iverpa.mpi.security;

import com.iverpa.mpi.model.Role;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

public class TokenBasedAuthentication extends AbstractAuthenticationToken {

    private final AuthorizedUser authorizedUser;

    public TokenBasedAuthentication(AuthorizedUser user, Set<Role> roles) {
        super(roles.stream().map(role -> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toSet()));
        this.authorizedUser = user;
    }

    public static AuthorizedUser unauthenticatedUser() {
        return new AuthorizedUser("unauthenticated", Set.of());
    }

    @Override
    public Object getCredentials() {
        return authorizedUser.username();
    }

    @Override
    public Object getPrincipal() {
        return authorizedUser;
    }
}
