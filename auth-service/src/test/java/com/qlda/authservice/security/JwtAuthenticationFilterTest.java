package com.qlda.authservice.security;

import com.qlda.authservice.entity.NguoiDung;
import com.qlda.authservice.entity.NhomQuyen;
import com.qlda.authservice.repository.NguoiDungRepository;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;
    @Mock
    private NguoiDungRepository nguoiDungRepository;
    @Mock
    private FilterChain filterChain;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldSkipAuthenticationWhenHeaderMissing() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, nguoiDungRepository);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).isAccessTokenValid(org.mockito.ArgumentMatchers.anyString());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldSkipAuthenticationWhenTokenInvalid() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, nguoiDungRepository);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(jwtService.isAccessTokenValid("invalid-token")).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(nguoiDungRepository, never()).findById(org.mockito.ArgumentMatchers.anyLong());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldSetSecurityContextWhenTokenValidAndUserActive() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, nguoiDungRepository);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer good-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        NguoiDung user = new NguoiDung();
        user.setId(1L);
        user.setUserName("admin");
        user.setTrangThai(1);
        NhomQuyen role = new NhomQuyen();
        role.setMaNhomQuyen("ADMIN");
        user.setNhomQuyen(role);

        when(jwtService.isAccessTokenValid("good-token")).thenReturn(true);
        when(jwtService.extractUserId("good-token")).thenReturn(1L);
        when(nguoiDungRepository.findById(1L)).thenReturn(Optional.of(user));

        filter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertTrue(authentication.getPrincipal() instanceof CurrentUserPrincipal);
        CurrentUserPrincipal principal = (CurrentUserPrincipal) authentication.getPrincipal();
        assertEquals(1L, principal.getUserId());
        assertEquals("admin", principal.getUsername());
        assertEquals(1, principal.getAuthorities().size());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotSetAuthenticationWhenUserInactive() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, nguoiDungRepository);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer good-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        NguoiDung user = new NguoiDung();
        user.setId(1L);
        user.setUserName("admin");
        user.setTrangThai(0);

        when(jwtService.isAccessTokenValid("good-token")).thenReturn(true);
        when(jwtService.extractUserId("good-token")).thenReturn(1L);
        when(nguoiDungRepository.findById(1L)).thenReturn(Optional.of(user));

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}
