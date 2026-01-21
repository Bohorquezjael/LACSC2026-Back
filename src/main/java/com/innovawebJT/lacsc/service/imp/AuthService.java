package com.innovawebJT.lacsc.service.imp;

import com.innovawebJT.lacsc.model.User;
import com.innovawebJT.lacsc.repository.UserRepository;
import com.innovawebJT.lacsc.security.SecurityUtils;
import com.innovawebJT.lacsc.service.IAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {

    private final UserRepository userRepository;

    @Override
    public User getCurrentUser() {

        String keycloakId = SecurityUtils.getKeycloakId();

        return userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() ->
                    new UsernameNotFoundException(
                        "Perfil de usuario no encontrado para el ID: " + keycloakId
                    )
                );
    }
}
