package com.chuseok22.eodaegoserver.domain.auth.service;

import com.chuseok22.eodaegoserver.domain.member.SocialType;
import com.chuseok22.eodaegoserver.global.exception.CustomException;
import com.chuseok22.eodaegoserver.global.exception.ErrorCode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FirebaseTokenVerifier {

  private final FirebaseAuth firebaseAuth;

  public FirebaseToken verify(String idToken) {
    try {
      return firebaseAuth.verifyIdToken(idToken);
    } catch (FirebaseAuthException e) {
      throw new CustomException(ErrorCode.FIREBASE_TOKEN_VERIFICATION_FAILED);
    }
  }

  public void assertSocialTypeMatches(FirebaseToken firebaseToken, SocialType requestedSocialType) {
    SocialType actualSocialType = resolveSocialType(firebaseToken);
    if (actualSocialType != requestedSocialType) {
      log.warn("소셜 로그인 provider 불일치: requested={}, actual={}", requestedSocialType, actualSocialType);
      throw new CustomException(ErrorCode.SOCIAL_TYPE_MISMATCH);
    }
  }

  private SocialType resolveSocialType(FirebaseToken firebaseToken) {
    Object firebaseClaim = firebaseToken.getClaims().get("firebase");
    if (!(firebaseClaim instanceof Map<?, ?> firebaseClaims)) {
      throw new CustomException(ErrorCode.SOCIAL_TYPE_MISMATCH);
    }

    Object signInProvider = firebaseClaims.get("sign_in_provider");
    if ("google.com".equals(signInProvider)) {
      return SocialType.GOOGLE;
    }
    if ("apple.com".equals(signInProvider)) {
      return SocialType.APPLE;
    }
    log.warn("인식할 수 없는 sign_in_provider: {}", signInProvider);
    throw new CustomException(ErrorCode.SOCIAL_TYPE_MISMATCH);
  }
}
