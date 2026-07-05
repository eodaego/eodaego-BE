package com.chuseok22.eodaegoserver.domain.auth.dto.request;

import com.chuseok22.eodaegoserver.domain.member.SocialType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
  @Schema(description = "Firebase Admin SDK로 검증할 소셜 로그인 ID Token", example = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjA5N...")
  @NotBlank String idToken,

  @Schema(description = "소셜 로그인 제공자. GOOGLE 또는 APPLE만 허용된다.", example = "GOOGLE")
  @NotNull SocialType socialType
) {

}
