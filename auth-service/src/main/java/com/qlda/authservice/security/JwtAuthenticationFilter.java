package com.qlda.authservice.security;

import com.qlda.authservice.entity.NguoiDung;
import com.qlda.authservice.repository.NguoiDungRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final NguoiDungRepository nguoiDungRepository;

    public JwtAuthenticationFilter(JwtService jwtService, NguoiDungRepository nguoiDungRepository) {
        this.jwtService = jwtService;
        this.nguoiDungRepository = nguoiDungRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7);
        if (!jwtService.isAccessTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        Long userId = jwtService.extractUserId(token);
        if (userId == null) {
            filterChain.doFilter(request, response);
            return;
        }

        nguoiDungRepository.findById(userId)
                .filter(user -> user.getTrangThai() != null && user.getTrangThai() == 1)
                .ifPresent(user -> setAuthentication(request, user));

        filterChain.doFilter(request, response);
    }

    private void setAuthentication(HttpServletRequest request, NguoiDung user) {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return;
        }
        String role = user.getNhomQuyen() == null ? "USER" : user.getNhomQuyen().getMaNhomQuyen();
        CurrentUserPrincipal principal = new CurrentUserPrincipal(
                user.getId(),
                user.getUserName(),
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
