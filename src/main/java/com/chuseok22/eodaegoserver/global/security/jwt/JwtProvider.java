package com.chuseok22.eodaegoserver.global.security.jwt;

import com.chuseok22.eodaegoserver.global.properties.JwtProperties;
import com.chuseok22.eodaegoserver.global.security.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {

  private final JwtProperties jwtProperties;
  private final SecretKey secretKey;

  public JwtProvider(JwtProperties jwtProperties) {
    this.jwtProperties = jwtProperties;
    this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secretKey().getBytes(StandardCharsets.UTF_8));
  }

  public String createAccessToken(UUID memberId, Role role) {
    return createToken(memberId, role, jwtProperties.accessExpMillis());
  }

  public String createRefreshToken(UUID memberId, Role role) {
    return createToken(memberId, role, jwtProperties.refreshExpMillis());
  }

  private String createToken(UUID memberId, Role role, long expMillis) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + expMillis);
    return Jwts.builder()
        .issuer(jwtProperties.issuer())
        .subject(memberId.toString())
        .claim("role", role.name())
        .issuedAt(now)
        .expiration(expiry)
        .signWith(secretKey)
        .compact();
  }

  public Claims parseClaims(String token) {
    return Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public Claims parseExpiredClaims(String token) {
    try {
      return parseClaims(token);
    } catch (ExpiredJwtException e) {
      return e.getClaims();
    }
  }
}
