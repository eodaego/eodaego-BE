package com.chuseok22.eodaegoserver.domain.auth.controller;

import com.chuseok22.apichangelog.annotation.ApiChangeLog;
import com.chuseok22.apichangelog.annotation.ApiChangeLogs;
import com.chuseok22.eodaegoserver.domain.auth.dto.request.LoginRequest;
import com.chuseok22.eodaegoserver.domain.auth.dto.request.ReissueRequest;
import com.chuseok22.eodaegoserver.domain.auth.dto.response.LoginResponse;
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
      ),
      @ApiChangeLog(
          date = "2026-07-10",
          author = ChangeLogAuthor.KIM_JAEHYEON,
          description = "요청에 deviceType/deviceId/fcmToken 필드 추가, 응답을 LoginResponse로 변경(nickname/userId/requiresAgreement 추가). 약관 동의는 더 이상 로그인 요청에서 처리하지 않으며, PATCH /members/me/agreements로 별도 처리한다.",
          issueUrl = "https://github.com/eodaego/eodaego-BE/issues/17"
      ),
      @ApiChangeLog(
          date = "2026-07-11",
          author = ChangeLogAuthor.KIM_JAEHYEON,
          description = "socialType을 idToken의 실제 로그인 제공자(sign_in_provider)와 대조 검증하는 로직 추가, 불일치 시 401 SOCIAL_TYPE_MISMATCH 반환",
          issueUrl = "https://github.com/eodaego/eodaego-BE/issues/17"
      ),
      @ApiChangeLog(
          date = "2026-07-11",
          author = ChangeLogAuthor.KIM_JAEHYEON,
          description = "fcmToken 갱신 조건 문구 정정 — 실제로는 요청에 값이 있을 때만 갱신되고 생략 시 기존 값이 유지되는데, 문서에는 매 로그인마다 갱신되는 것처럼 잘못 적혀있던 부분 수정",
          issueUrl = "https://github.com/eodaego/eodaego-BE/issues/17"
      )
  })
  @Operation(
      summary = "소셜 로그인",
      description = """
          Google/Apple 소셜 로그인 후 Firebase에서 발급받은 ID Token으로 로그인한다.

          - 백엔드는 전달받은 idToken을 Firebase Admin SDK로 검증한다(클라이언트 값을 그대로 신뢰하지 않음).
          - socialType은 클라이언트가 보낸 값이 아니라 idToken에서 검증된 실제 로그인 제공자(sign_in_provider)와 일치하는지 재확인하며, 불일치 시 로그인을 거부한다.
          - (socialType, providerId) 조합으로 기존 회원을 조회하며, 없으면 자동으로 신규 회원가입 처리한다.
          - 동일 이메일이라도 socialType이 다르면 별개 회원으로 취급한다(계정 자동 병합 없음).
          - deviceType/deviceId는 로그인마다 최신 값으로 갱신된다(기존 회원도 동일). fcmToken은 요청에 값이 있을 때만 갱신되며, 생략(null)하면 기존 값이 유지된다.
          - 응답의 firstLogin이 true이면 이번 요청에서 신규 가입된 회원이라는 뜻이다.
          - 응답의 requiresAgreement가 true이면 필수 약관(개인정보처리방침/위치정보/이용약관) 중 하나 이상 미동의 상태라는 뜻이며, 클라이언트는 약관 동의 화면을 띄운 뒤 PATCH /members/me/agreements를 호출해야 한다. 이 값은 신규 회원뿐 아니라 기존 회원이라도 약관 동의를 아직 완료하지 않았다면 매 로그인마다 다시 true로 내려간다.
          - 로그인 자체는 약관 동의 여부와 무관하게 항상 성공한다(약관 미동의를 이유로 로그인이 거부되지 않음).
          - 인증 없이 호출 가능하다(permitAll).
          """
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "로그인 성공, accessToken/refreshToken 발급"),
      @ApiResponse(responseCode = "400", description = """
          요청 바디 검증 실패. errorCode: INVALID_REQUEST

          - idToken 누락(공백)
          - socialType 누락 또는 GOOGLE/APPLE 외의 값
          - deviceType 누락 또는 IOS/ANDROID 외의 값
          - deviceId 누락(공백)
          """, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "401", description = """
          인증 실패. 아래 두 가지 중 하나이며 errorCode로 구분한다.

          - errorCode: FIREBASE_TOKEN_VERIFICATION_FAILED — Firebase ID Token 검증 실패(위변조, 만료, 발급자 불일치 등)
          - errorCode: SOCIAL_TYPE_MISMATCH — 요청한 socialType이 idToken에서 검증된 실제 로그인 제공자(sign_in_provider)와 일치하지 않음
          """, content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<LoginResponse> login(LoginRequest request);

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
