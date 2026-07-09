package com.chuseok22.eodaegoserver.domain.ai.controller;

import com.chuseok22.apichangelog.annotation.ApiChangeLog;
import com.chuseok22.apichangelog.annotation.ApiChangeLogs;
import com.chuseok22.eodaegoserver.domain.ai.dto.response.AiHealthCheckResponse;
import com.chuseok22.eodaegoserver.global.exception.ErrorResponse;
import com.chuseok22.eodaegoserver.global.swagger.ChangeLogAuthor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "AI", description = "AI 서버 상태 확인 API")
public interface AiHealthControllerDocs {

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2026-07-09",
          author = ChangeLogAuthor.BAEK_JIHOON,
          description = "AI 서버 헬스체크 엔드포인트 최초 구현",
          issueUrl = "https://github.com/eodaego/eodaego-BE/issues/15"
      )
  })
  @Operation(
      summary = "AI 서버 헬스체크",
      description = """
          내부망 AI 서버(eodaego-ai)의 상태를 확인한다.

          - 이 서버가 AI 서버의 /health 엔드포인트를 대신 호출해 결과를 확인한다(클라이언트가 AI 서버에 직접 접근하지 않음).
          - 인증 없이 호출 가능하다(permitAll, 모니터링 목적).
          - AI 서버가 정상 응답하면 status="UP"과 확인 시각을 반환한다.
          """
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "AI 서버 정상 응답"),
      @ApiResponse(responseCode = "503", description = "AI 서버 연결 실패(타임아웃, 커넥션 거부, AI 서버의 비정상 응답 포함). errorCode: AI_SERVER_UNAVAILABLE",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<AiHealthCheckResponse> checkAiServerHealth();
}
