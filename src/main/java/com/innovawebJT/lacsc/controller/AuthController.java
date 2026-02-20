package com.innovawebJT.lacsc.controller;

import com.innovawebJT.lacsc.dto.*;
import com.innovawebJT.lacsc.service.IUserService;
import com.innovawebJT.lacsc.service.imp.KeycloakService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final KeycloakService keycloakService;
    private final IUserService userService;
    private final JwtDecoder jwtDecoder;

    @Value("${auth.cookie.name:access_token}")
    private String cookieName;

    @Value("${auth.cookie.secure:true}")
    private boolean cookieSecure;

    @Value("${auth.cookie.same-site:none}")
    private String cookieSameSite;

    @Value("${auth.cookie.domain:}")
    private String cookieDomain;

    @Value("${auth.cookie.max-age-seconds:900}")
    private long cookieMaxAgeSeconds;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody RegisterDTO dto) {

        userService.validateRegistration(dto.email(), dto.badgeName());

        String keycloakId = keycloakService.createUser(
            dto.email(),
            dto.password(),
            dto.name(),
            dto.surname()
        );
        try {
            userService.createOrUpdateProfile(keycloakId, mapToUserProfile(dto));
        } catch (RuntimeException ex) {
            try {
                keycloakService.deleteUser(keycloakId);
            } catch (RuntimeException deleteEx) {
                log.warn("Failed to rollback Keycloak user {}", keycloakId, deleteEx);
            }
            throw ex;
        }

        return ResponseEntity.ok().build();
    }

    private UserProfileDTO mapToUserProfile(RegisterDTO dto) {

        EmergencyContactDTO contact = EmergencyContactDTO.builder()
                    .fullName(dto.emergencyContact().getName())
                    .relationship(dto.emergencyContact().getRelationship())
                    .phone(dto.emergencyContact().getCellphone())
                    .build();

	    return UserProfileDTO.builder()
	        .name(dto.name())
	        .surname(dto.surname())
	        .age(dto.age())
	        .badgeName(dto.badgeName())
	        .category(dto.category())
	        .institution(dto.institution())
	        .cellphone(dto.cellphone())
	        .gender(dto.gender())
	        .country(dto.country())
	        .email(dto.email())
	        .emergencyContact(contact)
	            .status(dto.status())
	        .build();
    }

    @PostMapping("/session")
    public ResponseEntity<Void> createSession(@RequestBody Map<String, String> body) {
        String token = body.get("access_token");
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        jwtDecoder.decode(token); // valida firma/expiración

        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(cookieName, token)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(Duration.ofSeconds(cookieMaxAgeSeconds));

        if (!cookieSameSite.isBlank()) {
            builder.sameSite(cookieSameSite);
        }
        if (!cookieDomain.isBlank()) {
            builder.domain(cookieDomain);
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, builder.build().toString())
                .build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie cookie = ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }
}
