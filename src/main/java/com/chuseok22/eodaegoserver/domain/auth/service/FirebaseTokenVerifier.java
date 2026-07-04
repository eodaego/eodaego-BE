package com.chuseok22.eodaegoserver.domain.auth.service;

import com.chuseok22.eodaegoserver.global.exception.CustomException;
import com.chuseok22.eodaegoserver.global.exception.ErrorCode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
}
