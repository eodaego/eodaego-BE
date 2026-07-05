package com.chuseok22.eodaegoserver.domain.auth.dto.request;

import com.chuseok22.eodaegoserver.domain.member.SocialType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
  @NotBlank String idToken,
  @NotNull SocialType socialType
) {

}
