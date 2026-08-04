package com.chuseok22.eodaegoserver.global.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

  @Schema(description = "에러 코드. 클라이언트는 이 값으로 에러 유형을 분기 처리한다.", example = "INVALID_REQUEST")
  private ErrorCode errorCode;

  @Schema(description = "사용자에게 표시 가능한 에러 메시지", example = "잘못된 요청입니다.")
  private String errorMessage;

  @Schema(description = "요청 필드별 오류 목록. 요청 바디 Bean Validation 실패 또는 요청 파라미터 "
      + "타입 변환 실패 시 포함되며, 각 항목은 field(필드명)와 reason(실패 사유)을 제공한다. "
      + "해당 오류가 없는 응답에서는 포함되지 않는다.")
  private List<FieldErrorDetail> fieldErrors;
}
