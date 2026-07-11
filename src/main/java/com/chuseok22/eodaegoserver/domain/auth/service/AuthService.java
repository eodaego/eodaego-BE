package com.chuseok22.eodaegoserver.domain.auth.service;

import com.chuseok22.eodaegoserver.domain.auth.dto.request.LoginRequest;
import com.chuseok22.eodaegoserver.domain.auth.dto.request.ReissueRequest;
import com.chuseok22.eodaegoserver.domain.auth.dto.response.LoginResponse;
import com.chuseok22.eodaegoserver.domain.auth.dto.response.ReissueResponse;
import com.chuseok22.eodaegoserver.domain.auth.dto.response.TokenResponse;
import com.chuseok22.eodaegoserver.domain.auth.entity.RefreshToken;
import com.chuseok22.eodaegoserver.domain.auth.repository.RefreshTokenRepository;
import com.chuseok22.eodaegoserver.domain.member.entity.Member;
import com.chuseok22.eodaegoserver.domain.member.repository.MemberRepository;
import com.chuseok22.eodaegoserver.global.exception.CustomException;
import com.chuseok22.eodaegoserver.global.exception.ErrorCode;
import com.chuseok22.eodaegoserver.global.properties.JwtProperties;
import com.chuseok22.eodaegoserver.global.security.Role;
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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

  private final MemberRepository memberRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final FirebaseTokenVerifier firebaseTokenVerifier;
  private final JwtProvider jwtProvider;
  private final JwtProperties jwtProperties;
  private final Clock clock;

  @Transactional
  public LoginResponse login(LoginRequest request) {
    FirebaseToken firebaseToken = firebaseTokenVerifier.verify(request.idToken());
    firebaseTokenVerifier.assertSocialTypeMatches(firebaseToken, request.socialType());

    Member member = memberRepository
        .findBySocialTypeAndProviderId(request.socialType(), firebaseToken.getUid())
        .orElseGet(() -> {
          Member newMember = Member.builder()
              .email(firebaseToken.getEmail())
              .nickname(resolveNickname(firebaseToken))
              .socialType(request.socialType())
              .providerId(firebaseToken.getUid())
              .role(Role.USER)
              .firstLogin(true)
              .deviceType(request.deviceType())
              .deviceId(request.deviceId())
              .fcmToken(request.fcmToken())
              .build();
          return memberRepository.save(newMember);
        });

    boolean firstLogin = member.isFirstLogin();
    if (firstLogin) {
      member.setFirstLogin(false);
    } else {
      member.setDeviceType(request.deviceType());
      member.setDeviceId(request.deviceId());
      if (request.fcmToken() != null) {
        member.setFcmToken(request.fcmToken());
      }
    }

    boolean requiresAgreement = !hasAgreedRequiredTerms(member);

    TokenResponse tokenResponse = issueTokens(member, firstLogin);
    log.info("로그인 성공: memberId={}, firstLogin={}, requiresAgreement={}",
        member.getId(), firstLogin, requiresAgreement);

    return LoginResponse.of(tokenResponse, requiresAgreement, member);
  }

  @Transactional
  public ReissueResponse reissue(ReissueRequest request) {
    Claims claims = jwtProvider.parseExpiredClaims(request.refreshToken());
    UUID memberId = UUID.fromString(claims.getSubject());
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    RefreshToken savedToken = refreshTokenRepository.findByMember(member)
        .orElseThrow(() -> new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

    if (!savedToken.getToken().equals(request.refreshToken())
        || savedToken.getExpiryDate().isBefore(LocalDateTime.now(clock))) {
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
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    refreshTokenRepository.deleteByMember(member);
    log.info("로그아웃 성공: memberId={}", memberId);
  }

  private boolean hasAgreedRequiredTerms(Member member) {
    return member.isPrivacyPolicyAgreed()
           && member.isLocationInfoAgreed()
           && member.isTermsOfServiceAgreed();
  }

  private TokenResponse issueTokens(Member member, boolean firstLogin) {
    String accessToken = jwtProvider.createAccessToken(member.getId(), member.getRole());
    String refreshToken = jwtProvider.createRefreshToken(member.getId(), member.getRole());
    LocalDateTime expiryDate = toExpiry(jwtProperties.refreshExpMillis());

    refreshTokenRepository.findByMember(member)
        .ifPresentOrElse(
            existing -> {
              existing.setToken(refreshToken);
              existing.setExpiryDate(expiryDate);
            },
            () -> refreshTokenRepository.save(
                RefreshToken.builder()
                    .member(member)
                    .token(refreshToken)
                    .expiryDate(expiryDate)
                    .build()
            )
        );

    return new TokenResponse(accessToken, refreshToken, firstLogin);
  }

  private String resolveNickname(FirebaseToken firebaseToken) {
    if (firebaseToken.getName() != null && !firebaseToken.getName().isBlank()) {
      return firebaseToken.getName();
    }
    return "회원" + firebaseToken.getUid().substring(0, Math.min(8, firebaseToken.getUid().length()));
  }

  private LocalDateTime toExpiry(long expMillis) {
    return LocalDateTime.ofInstant(Instant.now(clock).plusMillis(expMillis), clock.getZone());
  }
}
