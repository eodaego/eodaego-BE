package com.chuseok22.eodaegoserver.domain.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record NicknameResponse(

  @Schema(description = "변경된 닉네임", example = "어대탐험가")
  String nickname

) {
}