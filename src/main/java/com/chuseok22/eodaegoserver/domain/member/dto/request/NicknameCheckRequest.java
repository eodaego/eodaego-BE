package com.chuseok22.eodaegoserver.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record NicknameCheckRequest(

  @Schema(description = "중복 확인할 닉네임", example = "탐험가루카")
  @NotBlank(message = "닉네임은 비어 있을 수 없습니다.")
  @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하로 입력해야 합니다.")
  @Pattern(
    regexp = "^[가-힣a-zA-Z0-9]+$",
    message = "닉네임은 공백 없이 한글, 영어, 숫자만 사용할 수 있습니다."
  )
  String nickname

) {

}