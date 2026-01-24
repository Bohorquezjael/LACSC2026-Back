package com.innovawebJT.lacsc.service.imp;

import com.innovawebJT.lacsc.dto.TokenResponse;
import com.innovawebJT.lacsc.exception.DuplicateUserFieldException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KeycloakService {

    private final Keycloak keycloak;


    @Value("${keycloak.realm}")
    private String realm;

    public String createUser(String email, String password, String name, String surname) {

        UserRepresentation user = new UserRepresentation();
        user.setUsername(email);
        user.setEmail(email);
        user.setEnabled(true);
        user.setFirstName(name);
        user.setLastName(surname);
        user.setEmailVerified(false);

        Response response = keycloak.realm(realm)
                .users()
                .create(user);

        if (response.getStatus() == 409) {
            response.readEntity(String.class);
            throw new DuplicateUserFieldException("email", email);
        }

        if (response.getStatus() != 201) {
             String body = response.readEntity(String.class);
			 throw new RuntimeException(
                "Error creating user in Keycloak. Status: "
                + response.getStatus() + " Body: " + body
			 );
        }

        String userId = response.getLocation().getPath().replaceAll(".*/", "");

        // Password
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);

        keycloak.realm(realm)
                .users()
                .get(userId)
                .resetPassword(credential);

        // Verify email
        keycloak.realm(realm)
                .users()
                .get(userId)
                .executeActionsEmail(List.of("VERIFY_EMAIL"));

        return userId;
    }

    public void deleteUser(String userId) {
        keycloak.realm(realm)
                .users()
                .get(userId)
                .remove();
    }

//    public TokenResponse login(String username, String password) {
//
//        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
//        body.add("client_id", props.getClientId());
//        body.add("client_secret", props.getClientSecret());
//        body.add("grant_type", "password");
//        body.add("username", username);
//        body.add("password", password);
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//
//        HttpEntity<?> entity = new HttpEntity<>(body, headers);
//
//        ResponseEntity<Map> response = restTemplate.postForEntity(
//                props.getTokenUrl(),
//                entity,
//                Map.class
//        );
//
//        Map<String, Object> r = response.getBody();
//
//        return new TokenResponse(
//                (String) r.get("access_token"),
//                (String) r.get("refresh_token"),
//                ((Number) r.get("expires_in")).longValue()
//        );
//    }

}
