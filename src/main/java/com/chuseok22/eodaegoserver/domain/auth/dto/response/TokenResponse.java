package com.chuseok22.eodaegoserver.domain.auth.dto.response;

public record TokenResponse(
  String accessToken,
  String refreshToken,
  boolean firstLogin
) {

}
