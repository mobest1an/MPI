package com.iverpa.mpi.security;

import com.iverpa.mpi.model.Role;
import io.jsonwebtoken.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.security.PublicKey;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AuthTokenFilter extends HttpFilter {

    private final PublicKey publicKey;

    private static final Logger log = LoggerFactory.getLogger(AuthTokenFilter.class);

    public AuthTokenFilter(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {

        if (request.getHeader("Authorization") == null) {
            log.info("Authorization header is null, therefore request is denied");
            denyRequest(request, response, chain);
            return;
        }

        String token = request.getHeader("Authorization");
        String[] parts = token.split(" ");
        if (!parts[0].equals("Bearer")) {
            log.info("Bad JWT token {} was received, the user was rejected", token);
            denyRequest(request, response, chain);
            return;
        }
        token = parts[1];

        Claims claims;
        try {
            claims = validateToken(token);
            if (claims == null) {
                throw new IllegalArgumentException("Invalid JWT token");
            }
        } catch (Exception e) {
            log.debug("Bad JWT token {} was received, the user was rejected", token);
            denyRequest(request, response, chain);
            return;
        }

        String username = claims.get("username", String.class);
        List<String> roles = claims.get("roles", List.class);

        AuthorizedUser user = new AuthorizedUser(username, roles.stream().map(Role::valueOf).collect(Collectors.toSet()));
        authenticateRequest(request, response, chain, user);
    }

    private Claims validateToken(String authToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(this.publicKey).build().parseClaimsJws(authToken).getBody();
        } catch (MalformedJwtException ex) {
            throw new IllegalArgumentException("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            throw new IllegalArgumentException("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            throw new IllegalArgumentException("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("JWT claims string is empty.");
        }
    }

    private void authenticateRequest(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            AuthorizedUser user
    ) throws ServletException, IOException {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = new TokenBasedAuthentication(
                user,
                user.roles()
        );

        authentication.setAuthenticated(true);
        context.setAuthentication(authentication);

        chain.doFilter(request, response);
    }

    private void denyRequest(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = new TokenBasedAuthentication(
                TokenBasedAuthentication.unauthenticatedUser(),
                Set.of()
        );

        authentication.setAuthenticated(false);
        context.setAuthentication(authentication);

        chain.doFilter(request, response);
    }
}
