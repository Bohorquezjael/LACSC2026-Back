package com.innovawebJT.lacsc.security;

import org.springframework.security.core.Authentication;
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

    public static boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Authorities");
        auth.getAuthorities().forEach(a ->
                System.out.println(a.getAuthority())
        );
        if (auth == null) return false;

        return auth.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    public static boolean isAdminGeneral() {
        return hasRole("ADMIN_GENERAL");
    }

    public static boolean isAdminSesion() {
        return hasRole("ADMIN_SESSION");
    }

    public static java.util.List<String> getSessionRoles() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_S_"))
                .map(a -> a.replace("ROLE_", ""))
                .toList();
    }

    public static java.util.List<com.innovawebJT.lacsc.enums.SpecialSessions> getAllowedSessionsFromRoles() {
        return getSessionRoles().stream()
                .map(com.innovawebJT.lacsc.enums.SpecialSessions::valueOf)
                .toList();
    }

}
