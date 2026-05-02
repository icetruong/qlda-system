package com.qlda.authservice.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CurrentUserPrincipalTest {

    @Test
    void shouldExposeExpectedUserDetailFields() {
        CurrentUserPrincipal principal = new CurrentUserPrincipal(
                10L,
                "alice",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        assertEquals(10L, principal.getUserId());
        assertEquals("alice", principal.getUsername());
        assertEquals("", principal.getPassword());
        assertEquals(1, principal.getAuthorities().size());
        assertEquals("ROLE_ADMIN", principal.getAuthorities().iterator().next().getAuthority());
    }
}
