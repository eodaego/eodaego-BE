package com.chuseok22.eodaegoserver.domain.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record NicknameAvailabilityResponse(

  @Schema(description = "닉네임 사용 가능 여부", example = "true")
  boolean available

) {

}