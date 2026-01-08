package com.innovawebJT.lacsc.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static String getKeycloakId() {
        Object principal = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        if (principal instanceof Jwt jwt) {
            return jwt.getSubject(); // sub
        }

        throw new IllegalStateException("User not authenticated");
    }
}
