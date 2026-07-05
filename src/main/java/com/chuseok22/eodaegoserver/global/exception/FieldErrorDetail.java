package com.chuseok22.eodaegoserver.global.exception;

import io.swagger.v3.oas.annotations.media.Schema;

public record FieldErrorDetail(
  @Schema(description = "검증에 실패한 요청 필드명", example = "socialType")
  String field,

  @Schema(description = "검증 실패 사유", example = "널이어서는 안됩니다")
  String reason
) {

}
