package com.chuseok22.eodaegoserver.domain.weather.controller;

import com.chuseok22.apichangelog.annotation.ApiChangeLog;
import com.chuseok22.apichangelog.annotation.ApiChangeLogs;
import com.chuseok22.eodaegoserver.domain.weather.dto.response.WeatherResponse;
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

@Tag(name = "Weather", description = "날씨 조회 API")
public interface WeatherControllerDocs {

  @ApiChangeLogs({
    @ApiChangeLog(
      date = "2026-07-18",
      author = ChangeLogAuthor.KANG_JIYUN,
      description = "현재 날씨 조회 API 추가",
      issueUrl = "https://github.com/eodaego/eodaego-BE/issues/35"
    )
  })
  @Operation(
    summary = "현재 날씨 조회",
    description = """
          AI 서버에서 현재 날씨와 시간대별 날씨 예보를 조회한다.

          - 현재 기온, 습도, 강수 형태, 풍속, 하늘 상태를 반환한다.
          - 시간대별 기온, 강수 확률, 강수 형태, 하늘 상태를 반환한다.
          - Authorization: Bearer {accessToken} 헤더가 필요하다.
          """,
    security = @SecurityRequirement(name = "Bearer Token")
  )
  @ApiResponses({
    @ApiResponse(
      responseCode = "200",
      description = "현재 날씨 조회 성공"
    ),
    @ApiResponse(
      responseCode = "401",
      description = "Authorization 헤더가 없거나 accessToken이 유효하지 않음. errorCode: UNAUTHORIZED",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    ),
    @ApiResponse(
      responseCode = "503",
      description = "AI 서버 연결 실패 또는 날씨 조회 실패. errorCode: AI_SERVER_UNAVAILABLE",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
  })
  ResponseEntity<WeatherResponse> getCurrentWeather();
}