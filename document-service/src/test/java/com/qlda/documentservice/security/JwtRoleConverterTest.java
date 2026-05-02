package com.qlda.documentservice.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

class JwtRoleConverterTest {

    private final JwtRoleConverter converter = new JwtRoleConverter();

    @Test
    void convert_shouldMapRolesWithPrefix() {
        Jwt jwt = new Jwt(
            "token",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            Map.of("alg", "none"),
            Map.of("roles", List.of("ADMIN", "ROLE_CHUYEN_VIEN"))
        );

        List<String> authorities = converter.convert(jwt).stream().map(GrantedAuthority::getAuthority).sorted().toList();

        assertThat(authorities).containsExactly("ROLE_ADMIN", "ROLE_CHUYEN_VIEN");
    }

    @Test
    void convert_shouldReturnEmpty_whenNoRolesClaim() {
        Jwt jwt = new Jwt(
            "token",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            Map.of("alg", "none"),
            Map.of("sub", "u1")
        );

        assertThat(converter.convert(jwt)).isEmpty();
    }
}
