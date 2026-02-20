package com.innovawebJT.lacsc.security;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

@Component
public class JwtRolesConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    
    @Value("${jwt.principal-attribute}")
    private String principalAttribute;

    @Value("${jwt.auth.converter.resource-id}")
    private String resourceId;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = Stream
                .concat(jwtGrantedAuthoritiesConverter
                            .convert(jwt)
                            .stream(),
                        extractAuthoritiesRoles(jwt).stream())
                .toList();
        return new JwtAuthenticationToken(jwt, authorities, getPrincipalClaimName(jwt));
    }

    private String getPrincipalClaimName(Jwt jwt) {
        String claimName = JwtClaimNames.SUB;
        if (principalAttribute != null) {
            claimName = principalAttribute;
        }
        return jwt.getClaim(claimName);
    }

    private Collection<GrantedAuthority> extractAuthoritiesRoles(Jwt jwt) {

        Collection<GrantedAuthority> authorities = Stream.<GrantedAuthority>empty().toList();

        Map<String, Object> realmAccess = jwt.getClaim("realm_access");

        if (realmAccess != null && realmAccess.get("roles") instanceof Collection<?> rawRealmRoles) {

            authorities = rawRealmRoles.stream()
                    .filter(role -> role instanceof String)
                    .map(role -> (String) role)
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());
        }

        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");

        if (resourceAccess != null) {

            Object resourceObj = resourceAccess.get(resourceId);

            if (resourceObj instanceof Map<?, ?> resource) {

                Object rolesObj = resource.get("roles");

                if (rolesObj instanceof Collection<?> rawRoles) {

                    authorities = Stream.concat(
                            authorities.stream(),
                            rawRoles.stream()
                                    .filter(role -> role instanceof String)
                                    .map(role -> (String) role)
                                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    ).collect(Collectors.toList());
                }
            }
        }

        return authorities;
    }

}
