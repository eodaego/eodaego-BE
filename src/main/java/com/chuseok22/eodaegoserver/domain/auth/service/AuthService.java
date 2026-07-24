package com.chuseok22.eodaegoserver.domain.auth.service;

import com.chuseok22.eodaegoserver.domain.auth.dto.request.LoginRequest;
import com.chuseok22.eodaegoserver.domain.auth.dto.request.ReissueRequest;
import com.chuseok22.eodaegoserver.domain.auth.dto.response.LoginResponse;
import com.chuseok22.eodaegoserver.domain.auth.dto.response.ReissueResponse;
import com.chuseok22.eodaegoserver.domain.auth.entity.RefreshToken;
import com.chuseok22.eodaegoserver.domain.auth.repository.RefreshTokenRepository;
import com.chuseok22.eodaegoserver.domain.member.entity.Member;
import com.chuseok22.eodaegoserver.domain.member.repository.MemberRepository;
import com.chuseok22.eodaegoserver.global.exception.CustomException;
import com.chuseok22.eodaegoserver.global.exception.ErrorCode;
import com.chuseok22.eodaegoserver.global.properties.JwtProperties;
import com.chuseok22.eodaegoserver.global.security.jwt.JwtProvider;
import com.google.firebase.auth.FirebaseToken;
import io.jsonwebtoken.Claims;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

  private static final int MAX_NICKNAME_ATTEMPTS = 30;
  private static final int MAX_SOCIAL_PROVIDER_RETRIES = 3;

  private static final String NICKNAME_CONSTRAINT = "uk_member_nickname";
  private static final String SOCIAL_PROVIDER_CONSTRAINT = "uk_member_social_provider";

  private final MemberRepository memberRepository;
  private final LoginTransactionService loginTransactionService;

  private final RefreshTokenRepository refreshTokenRepository;
  private final FirebaseTokenVerifier firebaseTokenVerifier;
  private final JwtProvider jwtProvider;
  private final JwtProperties jwtProperties;
  private final Clock clock;

  public LoginResponse login(LoginRequest request) {
    FirebaseToken firebaseToken = firebaseTokenVerifier.verify(request.idToken());
    firebaseTokenVerifier.assertSocialTypeMatches(firebaseToken, request.socialType());

    int nicknameAttempts = 0;
    int socialProviderRetries = 0;

    while (nicknameAttempts < MAX_NICKNAME_ATTEMPTS) {
      try {
        return loginTransactionService.login(request, firebaseToken);
      } catch (DataIntegrityViolationException e) {
        if (isConstraintViolation(e, NICKNAME_CONSTRAINT)) {
          nicknameAttempts++;
          log.warn("랜덤 닉네임 충돌: attempt={}/{}", nicknameAttempts, MAX_NICKNAME_ATTEMPTS);
          continue;
        }

        if (isConstraintViolation(e, SOCIAL_PROVIDER_CONSTRAINT) && socialProviderRetries < MAX_SOCIAL_PROVIDER_RETRIES) {
          socialProviderRetries++;
          log.info("동일 소셜 계정 동시 생성 충돌: retry={}/{}", socialProviderRetries, MAX_SOCIAL_PROVIDER_RETRIES);
          continue;
        }
        throw e;
      }
    }

    throw new CustomException(ErrorCode.NICKNAME_GENERATION_FAILED);
  }

  @Transactional
  public ReissueResponse reissue(ReissueRequest request) {
    Claims claims = jwtProvider.parseExpiredClaims(request.refreshToken());
    UUID memberId = UUID.fromString(claims.getSubject());
    Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    RefreshToken savedToken = refreshTokenRepository.findByMember(member).orElseThrow(() -> new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

    if (!savedToken.getToken().equals(request.refreshToken()) || savedToken.getExpiryDate().isBefore(LocalDateTime.now(clock))) {
      throw new CustomException(ErrorCode.REFRESH_TOKEN_MISMATCH);
    }

    String accessToken = jwtProvider.createAccessToken(member.getId(), member.getRole());
    String newRefreshToken = jwtProvider.createRefreshToken(member.getId(), member.getRole());
    savedToken.setToken(newRefreshToken);
    savedToken.setExpiryDate(toExpiry(jwtProperties.refreshExpMillis()));

    log.info("토큰 재발급 성공: memberId={}", member.getId());
    return new ReissueResponse(accessToken, newRefreshToken);
  }

  @Transactional
  public void logout(UUID memberId) {
    Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    refreshTokenRepository.deleteByMember(member);
    log.info("로그아웃 성공: memberId={}", memberId);
  }

  private boolean isConstraintViolation(Throwable throwable, String expectedConstraintName) {
    Throwable current = throwable;

    while (current != null) {
      if (current instanceof ConstraintViolationException exception) {
        String actualConstraintName = exception.getConstraintName();

        return actualConstraintName != null && expectedConstraintName.equalsIgnoreCase(actualConstraintName);
      }
      current = current.getCause();
    }

    return false;
  }

  private LocalDateTime toExpiry(long expMillis) {
    return LocalDateTime.ofInstant(Instant.now(clock).plusMillis(expMillis), clock.getZone());
  }
}
