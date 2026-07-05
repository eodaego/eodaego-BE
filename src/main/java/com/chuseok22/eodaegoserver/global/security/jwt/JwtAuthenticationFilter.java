package com.chuseok22.eodaegoserver.global.security.jwt;

import com.chuseok22.eodaegoserver.global.security.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String HEADER_PREFIX = "Authorization";
  private static final String TOKEN_HEADER_PREFIX = "Bearer ";

  private final JwtProvider jwtProvider;

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws ServletException, IOException {
    String token = resolveToken(request);

    if (token != null) {
      try {
        Claims claims = jwtProvider.parseClaims(token);
        UUID memberId = UUID.fromString(claims.getSubject());
        Role role = Role.valueOf(claims.get("role", String.class));

        Authentication authentication = new UsernamePasswordAuthenticationToken(
          memberId, null, List.of(new SimpleGrantedAuthority("ROLE_" + role.name())));
        SecurityContextHolder.getContext().setAuthentication(authentication);
      } catch (JwtException | IllegalArgumentException e) {
        log.warn("JWT 인증 실패: {}", e.getMessage());
        SecurityContextHolder.clearContext();
      }
    }
    filterChain.doFilter(request, response);
  }

  private String resolveToken(HttpServletRequest request) {
    String header = request.getHeader(HEADER_PREFIX);
    if (header != null && header.startsWith(TOKEN_HEADER_PREFIX)) {
      return header.substring(TOKEN_HEADER_PREFIX.length());
    }
    return null;
  }
}
