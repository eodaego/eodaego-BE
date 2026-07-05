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

  @Schema(description = "요청 바디 검증(Bean Validation) 실패 시에만 포함되는 필드별 오류 목록. 검증 실패가 아닌 에러(인증 실패 등)에서는 응답에 아예 포함되지 않는다.")
  private List<FieldErrorDetail> fieldErrors;
}