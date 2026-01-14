package com.innovawebJT.lacsc.controller;

import com.innovawebJT.lacsc.dto.LoginRequest;
import com.innovawebJT.lacsc.dto.RegisterDTO;
import com.innovawebJT.lacsc.dto.TokenResponse;
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
            .build();

    userService.createOrUpdateProfile(keycloakId, profile);

    return ResponseEntity.ok().build();
}

//    @PostMapping("/login")
//    public TokenResponse login(@RequestBody LoginRequest request) {
//        return keycloakService.login(
//                request.username(),
//                request.password()
//        );
//    }

}