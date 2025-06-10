package com.ipaam.ai.transfer.util;

import com.ipaam.ai.transfer.model.KeycloakUserDetails;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, JwtAuthenticationToken> {
    @Override
    public JwtAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        KeycloakUserDetails userDetails = extractUserDetails(jwt);
        return new JwtAuthenticationToken(jwt, authorities, userDetails.getUsername());
    }

    private KeycloakUserDetails extractUserDetails(Jwt jwt) {
        String username = jwt.getClaim("email");
        String nationalCode = jwt.getClaim("national_code");
        String birthdate = jwt.getClaim("birthdate");

        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);

        return new KeycloakUserDetails(username, nationalCode, birthdate, authorities);
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        // Extract roles from both realm_access and resource_access if needed
        Set<String> roles = new HashSet<>();

        // Realm roles
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            roles.addAll((Collection<String>) realmAccess.get("roles"));
        }

        // Client roles (from the 'roles' claim in your token)
        if (jwt.hasClaim("roles")) {
            roles.addAll(jwt.getClaimAsStringList("roles"));
        }

        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }
}
