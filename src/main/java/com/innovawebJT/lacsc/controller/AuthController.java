package com.innovawebJT.lacsc.controller;

import com.innovawebJT.lacsc.dto.*;
import com.innovawebJT.lacsc.service.IUserService;
import com.innovawebJT.lacsc.service.imp.KeycloakService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
@Slf4j
public class AuthController {
	private final KeycloakService keycloakService;
    private final IUserService userService;

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
        UserProfileDTO profile = UserProfileDTO.builder()
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

        return profile;
    }
}
