package com.innovawebJT.lacsc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import lombok.RequiredArgsConstructor;

import static org.springframework.security.config.Customizer.withDefaults;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtRolesConverter jwtRolesConverter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(http -> http
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/h2-console/**")
                        .hasRole("admin-client")
                        .anyRequest().authenticated())
                .oauth2Login(withDefaults())
                .oauth2ResourceServer(oauth -> {
                    oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtRolesConverter));
                })
                .sessionManagement(sessionMg -> sessionMg.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .build();
    }
}
