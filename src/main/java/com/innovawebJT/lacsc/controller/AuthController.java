package com.innovawebJT.lacsc.controller;

import com.innovawebJT.lacsc.dto.RegisterDTO;
import com.innovawebJT.lacsc.dto.UserProfileDTO;
import com.innovawebJT.lacsc.service.IUserService;
import com.innovawebJT.lacsc.service.imp.KeycloakService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {
	private final KeycloakService keycloakService;
    private final IUserService userService;

    @PostMapping("/register")
public ResponseEntity<Void> register(@RequestBody RegisterDTO dto) {

    String keycloakId = keycloakService.createUser(
            dto.email(),
            dto.password(),
            dto.name(),
            dto.surname()
    );

    UserProfileDTO profileDTO = new UserProfileDTO(
            dto.badgeName(),
            dto.category(),
            dto.institution()
    );

    userService.createOrUpdateProfile(keycloakId, profileDTO);

    return ResponseEntity.ok().build();
}
}