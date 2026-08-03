package com.chuseok22.eodaegoserver.domain.congestion.controller;

import com.chuseok22.apichangelog.annotation.ApiChangeLog;
import com.chuseok22.apichangelog.annotation.ApiChangeLogs;
import com.chuseok22.eodaegoserver.domain.congestion.dto.response.CongestionResponse;
import com.chuseok22.eodaegoserver.global.exception.ErrorResponse;
import com.chuseok22.eodaegoserver.global.swagger.ChangeLogAuthor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Congestion", description = "공원 혼잡도 조회 API")
public interface CongestionControllerDocs {

  @ApiChangeLogs({
    @ApiChangeLog(
      date = "2026-08-03",
      author = ChangeLogAuthor.KIM_JAEHYEON,
      description = "공원 혼잡도 조회 API 최초 작성",
      issueUrl = "https://github.com/eodaego/eodaego-BE/issues/51"
    )
  })
  @Operation(
    summary = "현재 공원 혼잡도 조회",
    description = """
          서울어린이대공원의 현재 혼잡도를 조회한다. 홈 화면 상단의 "오늘 날씨 + 혼잡도" 표시에 사용한다.

          **응답 필드**
          - `level`: 혼잡도 등급. `RELAXED`(여유) / `NORMAL`(보통) / `SLIGHTLY_CROWDED`(약간 붐빔) / `CROWDED`(붐빔) 네 가지.
            색상·아이콘 분기는 이 값으로 한다.
          - `label`: 등급의 한글 표기(`여유`, `보통`, `약간 붐빔`, `붐빔`). 화면에 그대로 노출해도 되는 짧은 라벨이다.
          - `collectedAt`: 데이터를 수집한 시각.

          **level이 null로 내려오는 경우**
          원본 데이터 제공처(서울시)가 기존 4단계 외에 새로운 등급을 추가하면 `level`이 `null`이 된다.
          이때도 `label`에는 원본 문자열이 그대로 담기므로, `level`이 null이면 색상 분기는 기본값으로 두고
          `label`만 표시하면 된다. 이 경우에도 HTTP 상태는 200이다.

          **collectedAt 해석 시 주의**
          이 값은 AI 서버가 원본 데이터를 가져온 시각이며, 서울시가 산정한 데이터 기준 시각과는 차이가 있다.
          원본 자체의 지연과 수집 주기가 더해져 실제 기준 시각보다 수십 분 늦을 수 있으므로
          "현재 시각의 혼잡도"로 단정해 표기하지 않는다.

          - Authorization: Bearer {accessToken} 헤더가 필요하다.
          """,
    security = @SecurityRequirement(name = "Bearer Token")
  )
  @ApiResponses({
    @ApiResponse(
      responseCode = "200",
      description = "혼잡도 조회 성공"
    ),
    @ApiResponse(
      responseCode = "401",
      description = "Authorization 헤더가 없거나 accessToken이 유효하지 않음. errorCode: UNAUTHORIZED",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    ),
    @ApiResponse(
      responseCode = "503",
      description = "AI 서버 연결 실패, 또는 AI 서버는 정상이지만 수집된 혼잡도 데이터가 한 건도 없음. errorCode: AI_SERVER_UNAVAILABLE",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
  })
  ResponseEntity<CongestionResponse> getCurrentCongestion();
}
