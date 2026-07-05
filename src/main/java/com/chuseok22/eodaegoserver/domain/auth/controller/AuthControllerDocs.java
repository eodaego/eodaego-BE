package com.chuseok22.eodaegoserver.domain.auth.controller;

import com.chuseok22.apichangelog.annotation.ApiChangeLog;
import com.chuseok22.apichangelog.annotation.ApiChangeLogs;
import com.chuseok22.eodaegoserver.domain.auth.dto.request.LoginRequest;
import com.chuseok22.eodaegoserver.domain.auth.dto.request.ReissueRequest;
import com.chuseok22.eodaegoserver.domain.auth.dto.response.TokenResponse;
import com.chuseok22.eodaegoserver.global.exception.ErrorResponse;
import com.chuseok22.eodaegoserver.global.swagger.ChangeLogAuthor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.ResponseEntity;

@Tag(name = "Auth", description = "회원 소셜 로그인 및 토큰 관리 API")
public interface AuthControllerDocs {

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2026-07-05",
          author = ChangeLogAuthor.BAEK_JIHOON,
          description = "Swagger 문서 상세화 — idToken/socialType 필드 설명 및 example 추가, 에러 응답(ErrorResponse) 스키마 연결",
          issueUrl = "https://github.com/eodaego/eodaego-BE/issues/10"
      )
  })
  @Operation(
      summary = "소셜 로그인",
      description = """
          Google/Apple 소셜 로그인 후 Firebase에서 발급받은 ID Token으로 로그인한다.

          - 백엔드는 전달받은 idToken을 Firebase Admin SDK로 검증한다(클라이언트 값을 그대로 신뢰하지 않음).
          - (socialType, providerId) 조합으로 기존 회원을 조회하며, 없으면 자동으로 신규 회원가입 처리한다.
          - 동일 이메일이라도 socialType이 다르면 별개 회원으로 취급한다(계정 자동 병합 없음).
          - 응답의 firstLogin이 true이면 이번 요청에서 신규 가입된 회원이라는 뜻이다.
          - 인증 없이 호출 가능하다(permitAll).
          """
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "로그인 성공, accessToken/refreshToken 발급"),
      @ApiResponse(responseCode = "400", description = """
          요청 바디 검증 실패. errorCode: INVALID_REQUEST

          - idToken 누락(공백)
          - socialType 누락 또는 GOOGLE/APPLE 외의 값
          """, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "401", description = "Firebase ID Token 검증 실패(위변조, 만료, 발급자 불일치 등). errorCode: FIREBASE_TOKEN_VERIFICATION_FAILED",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<TokenResponse> login(LoginRequest request);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2026-07-05",
          author = ChangeLogAuthor.BAEK_JIHOON,
          description = "Swagger 문서 상세화 — refreshToken 필드 설명 및 example 추가, errorCode별 에러 원인 명시",
          issueUrl = "https://github.com/eodaego/eodaego-BE/issues/10"
      )
  })
  @Operation(
      summary = "토큰 재발급",
      description = """
          만료되었거나 만료가 임박한 accessToken을 refreshToken으로 재발급한다.

          - 전달받은 refreshToken이 DB에 저장된 값과 일치하고, 만료되지 않았는지 모두 확인한다.
          - 재발급 시 refreshToken도 함께 새로 발급(Rotation)하며, 기존 refreshToken은 즉시 무효화된다.
          - 회원당 refreshToken은 1개만 유지되므로(단일 기기 로그인), 새 기기에서 로그인하면 이전 기기의 refreshToken은 자동으로 무효화된다.
          - 인증 없이 호출 가능하다(permitAll, refreshToken 자체로 검증).
          """
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "재발급 성공, 새 accessToken/refreshToken 발급"),
      @ApiResponse(responseCode = "400", description = "요청 바디 검증 실패(refreshToken 누락). errorCode: INVALID_REQUEST",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "401", description = """
          refreshToken 검증 실패. 아래 세 가지 중 하나이며 errorCode로 구분한다.

          - errorCode: INVALID_TOKEN — refreshToken 자체가 위변조되었거나 서명이 유효하지 않음(JWT 파싱 실패)
          - errorCode: REFRESH_TOKEN_NOT_FOUND — 서버(DB)에 저장된 refreshToken이 없음(로그아웃되었거나 다른 기기 로그인으로 무효화됨)
          - errorCode: REFRESH_TOKEN_MISMATCH — 저장된 값과 일치하지 않거나 만료됨
          """, content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<TokenResponse> reissue(ReissueRequest request);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2026-07-05",
          author = ChangeLogAuthor.BAEK_JIHOON,
          description = "Swagger 문서 상세화 — 에러 응답(ErrorResponse) 스키마 연결 및 errorCode 명시",
          issueUrl = "https://github.com/eodaego/eodaego-BE/issues/10"
      )
  })
  @Operation(
      summary = "로그아웃",
      description = """
          현재 인증된 회원의 refreshToken을 DB에서 삭제하여 무효화한다.

          - accessToken은 자체 만료 전까지는 계속 유효하다(서버 측 즉시 폐기 없음). 클라이언트가 로컬에서 삭제해야 한다.
          - Authorization: Bearer {accessToken} 헤더가 반드시 필요하다.
          """,
      security = @SecurityRequirement(name = "Bearer Token")
  )
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "로그아웃 성공, 응답 바디 없음"),
      @ApiResponse(responseCode = "401", description = "Authorization 헤더가 없거나 accessToken이 유효하지 않음. errorCode: UNAUTHORIZED",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<Void> logout(UUID memberId);
}
