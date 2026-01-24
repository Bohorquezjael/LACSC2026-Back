package com.innovawebJT.lacsc.service.imp;

import com.innovawebJT.lacsc.exception.DuplicateUserFieldException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KeycloakService {

    private final Keycloak keycloak;


    @Value("${keycloak.realm}")
    private String realm;

    public String createUser(String email, String password, String name, String surname) {

        if (realm == null || realm.isBlank()) {
            throw new IllegalStateException("Configuración inválida: 'keycloak.realm' está vacío.");
        }

        UserRepresentation user = new UserRepresentation();
        user.setUsername(email);
        user.setEmail(email);
        user.setEnabled(true);
        user.setFirstName(name);
        user.setLastName(surname);
        user.setEmailVerified(false);

        String userId;
        try (Response response = keycloak.realm(realm)
                .users()
                .create(user)) {

            int status = response.getStatus();
            String body = response.hasEntity() ? response.readEntity(String.class) : null;

            if (status == 409) {
                // Ojo: 409 no siempre significa "email". Puede ser username u otro atributo en conflicto.
                throw new DuplicateUserFieldException("email", email);
            }

            if (status != 201) {
                throw new RuntimeException(
                        "Error creando usuario en Keycloak. Status: " + status + " Body: " + body
                );
            }

            URI location = response.getLocation();
            if (location == null || location.getPath() == null || location.getPath().isBlank()) {
                throw new IllegalStateException(
                        "Keycloak devolvió 201 pero sin header Location; no se puede obtener el userId. Body: " + body
                );
            }

            userId = location.getPath().replaceAll(".*/", "");
            if (userId.isBlank()) {
                throw new IllegalStateException("No se pudo extraer userId desde Location: " + location);
            }
        }

        // Password
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);

        try {
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
        } catch (RuntimeException ex) {
            // Si algo falla después de crear el usuario, evitamos dejar "usuarios a medias".
            // Puedes decidir si esto aplica a tu negocio; al menos reduce inconsistencias.
            try {
                deleteUser(userId);
            } catch (RuntimeException ignored) {
                // no sobrescribimos la excepción original
            }
            throw ex;
        }
    }

    public void deleteUser(String userId) {
        keycloak.realm(realm)
                .users()
                .get(userId)
                .remove();
    }

    // ... existing code ...
}
