package com.chuseok22.eodaegoserver.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ReissueRequest(
  @Schema(description = "기존 로그인/재발급 시 발급받은 refreshToken", example = "eyJhbGciOiJIUzI1NiJ9...")
  @NotBlank String refreshToken
) {

}
