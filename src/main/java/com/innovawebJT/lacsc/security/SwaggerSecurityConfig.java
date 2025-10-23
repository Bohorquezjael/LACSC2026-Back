package com.innovawebJT.lacsc.security;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerSecurityConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LACSC API")
                        .version("1.0")
                        .description("API Documentation secured with Keycloak"))
                .addSecurityItem(new SecurityRequirement().addList("keycloak"))
                .components(new Components()
                        .addSecuritySchemes("keycloak",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.OAUTH2)
                                        .description("OAuth2 flow using Keycloak")
                                        .flows(new OAuthFlows()
                                                .authorizationCode(new OAuthFlow()
                                                        .authorizationUrl("http://localhost:8088/realms/lacsc2026-dev/protocol/openid-connect/auth")
                                                        .tokenUrl("http://localhost:8088/realms/lacsc2026-dev/protocol/openid-connect/token")
                                                        .scopes(new Scopes()
                                                                .addString("openid", "OpenID Connect scope")
                                                                .addString("profile", "User profile")
                                                                .addString("email", "User email"))))));
    }
}
