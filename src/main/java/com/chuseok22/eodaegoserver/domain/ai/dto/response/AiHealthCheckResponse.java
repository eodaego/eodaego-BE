package com.chuseok22.eodaegoserver.domain.ai.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record AiHealthCheckResponse(

    @Schema(description = "AI 서버 상태. 호출이 실패하면 예외로 처리되어 이 응답 자체가 생성되지 않으므로, 정상 응답에서는 항상 \"UP\"이다.", example = "UP")
    String status,

    @Schema(description = "이 서버가 AI 서버 상태를 확인한 시각", example = "2026-07-09T10:15:30")
    LocalDateTime checkedAt
) {

}
