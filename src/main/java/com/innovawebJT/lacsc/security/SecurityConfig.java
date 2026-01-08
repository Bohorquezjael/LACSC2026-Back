package com.innovawebJT.lacsc.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
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
        @Order(0)
        public SecurityFilterChain authSecurity(HttpSecurity http) throws Exception {
                return http
                                .securityMatcher("/auth/**")
                                .csrf(AbstractHttpConfigurer::disable)
                                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                                .build();
        }

        @Bean
        @Order(1)
        public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
                return httpSecurity
                                .securityMatcher("/api/**")
                                .csrf(AbstractHttpConfigurer::disable)
                                .authorizeHttpRequests(http -> http.anyRequest().authenticated())
                                .oauth2ResourceServer(oauth -> oauth
                                                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtRolesConverter)))
                                .sessionManagement(sessionMg -> sessionMg
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .build();
        }

@Bean
@Order(2)
public SecurityFilterChain swaggerSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                        .securityMatcher("/swagger-ui/**", "/swagger-ui.html",
                                        "/v3/api-docs/**", "/swagger-resources/**",
                                        "/webjars/**")
                        .csrf(AbstractHttpConfigurer::disable)
                        .authorizeHttpRequests(http -> http.anyRequest().authenticated())
                        .oauth2Login(withDefaults())
                        .build();
}

        @Bean
        @Order(3)
        public SecurityFilterChain devSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {
                return httpSecurity
                                .csrf(AbstractHttpConfigurer::disable)
                                .headers(headers -> headers
                                                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                                .authorizeHttpRequests(http -> http
                                                .requestMatchers("/h2-console/**").authenticated()
                                                .anyRequest().authenticated())
                                .oauth2Login(withDefaults())
                                .logout(logout -> logout
                                                .logoutSuccessUrl("/")
                                                .invalidateHttpSession(true)
                                                .clearAuthentication(true)
                                                .deleteCookies("JSESSIONID"))
                                .build();
        }
}