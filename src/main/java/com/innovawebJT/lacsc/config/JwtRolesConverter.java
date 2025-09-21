package com.innovawebJT.lacsc.config;

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
        Map<String, Object> resourceAccess;
        Map<String, Object> resource;
        Collection<String> roles;

        if(jwt.getClaim("resource_access") == null){
            return Stream.<GrantedAuthority>of().toList();
        }
        resourceAccess = jwt.getClaim("resource_access");

        Object resourceObj = resourceAccess.get(resourceId);
        if (!(resourceObj instanceof Map)) {
            return Stream.<GrantedAuthority>of().toList();
        }
        resource = (Map<String, Object>) resourceObj;

        Object rolesObj = resource.get("roles");
        if (!(rolesObj instanceof Collection)) {
            return Stream.<GrantedAuthority>of().toList();
        }
        // Ensure all elements are String
        Collection<?> rawRoles = (Collection<?>) rolesObj;
        roles = rawRoles.stream()
                .filter(e -> e instanceof String)
                .map(e -> (String) e)
                .collect(Collectors.toList());
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }
}
