package com.chuseok22.eodaegoserver.domain.member.controller;

import com.chuseok22.apichangelog.annotation.ApiChangeLog;
import com.chuseok22.apichangelog.annotation.ApiChangeLogs;
import com.chuseok22.eodaegoserver.domain.member.dto.request.AgreementRequest;
import com.chuseok22.eodaegoserver.domain.member.dto.response.AgreementResponse;
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

@Tag(name = "Member", description = "회원 약관 동의 관리 API")
public interface MemberControllerDocs {

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2026-07-10",
          author = ChangeLogAuthor.KIM_JAEHYEON,
          description = "약관 동의 여부 조회 API 최초 작성",
          issueUrl = "https://github.com/eodaego/eodaego-BE/issues/17"
      )
  })
  @Operation(
      summary = "약관 동의 여부 조회",
      description = """
          현재 인증된 회원의 약관 동의 상태를 조회한다.

          - 개인정보처리방침/위치정보/이용약관 3개는 필수 약관이며, 마케팅은 선택 약관이다.
          - 로그인 응답의 requiresAgreement가 true였다면, 이 API로 현재 어떤 항목이 미동의 상태인지 확인할 수 있다.
          - Authorization: Bearer {accessToken} 헤더가 반드시 필요하다.
          """,
      security = @SecurityRequirement(name = "Bearer Token")
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "401", description = "Authorization 헤더가 없거나 accessToken이 유효하지 않음. errorCode: UNAUTHORIZED",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 회원. errorCode: MEMBER_NOT_FOUND",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<AgreementResponse> getAgreement(UUID memberId);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2026-07-10",
          author = ChangeLogAuthor.KIM_JAEHYEON,
          description = "약관 동의 여부 수정 API 최초 작성",
          issueUrl = "https://github.com/eodaego/eodaego-BE/issues/17"
      ),
      @ApiChangeLog(
          date = "2026-07-11",
          author = ChangeLogAuthor.KIM_JAEHYEON,
          description = "termsAgreedAt이 매 수정마다 갱신되던 버그 수정 — 필수 약관 최초 동의 시점만 기록하고 이후 호출에서는 보존한다. marketingAgreedAt은 동의 상태가 false→true로 바뀔 때만 갱신된다.",
          issueUrl = "https://github.com/eodaego/eodaego-BE/issues/17"
      )
  })
  @Operation(
      summary = "약관 동의 여부 수정",
      description = """
          현재 인증된 회원의 약관 동의 상태를 갱신한다.

          - 개인정보처리방침/위치정보/이용약관 3개는 all-or-nothing이다. 셋 다 true로만 보낼 수 있으며(부분 동의·철회 불가), 하나라도 false거나 누락되면 요청 자체가 거부된다.
          - 마케팅은 true/false 자유롭게 보낼 수 있다(선택값, 언제든 켜고 끌 수 있음).
          - 성공 시 응답 바디는 없다(204). 갱신된 값을 확인하려면 GET /members/me/agreements를 다시 호출한다.
          - Authorization: Bearer {accessToken} 헤더가 반드시 필요하다.
          """,
      security = @SecurityRequirement(name = "Bearer Token")
  )
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "수정 성공, 응답 바디 없음"),
      @ApiResponse(responseCode = "400", description = """
          요청 바디 검증 실패. errorCode: INVALID_REQUEST

          - privacyPolicyAgreed/locationInfoAgreed/termsOfServiceAgreed 중 누락되었거나 true가 아닌 값이 있음
          """, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "401", description = "Authorization 헤더가 없거나 accessToken이 유효하지 않음. errorCode: UNAUTHORIZED",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 회원. errorCode: MEMBER_NOT_FOUND",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<Void> updateAgreement(UUID memberId, AgreementRequest request);

  @ApiChangeLogs({
    @ApiChangeLog(
      date = "2026-07-12",
      author = ChangeLogAuthor.KANG_JIYUN,
      description = "회원탈퇴 API 추가",
      issueUrl = "https://github.com/eodaego/eodaego-BE/issues/19"
    )
  })
  @Operation(
    summary = "회원탈퇴",
    description = """
        현재 인증된 회원을 탈퇴 처리한다.

        - Authorization: Bearer {accessToken} 헤더가 필요하다.
        - 현재 저장된 refreshToken을 삭제한다.
        - 회원 정보를 DB에서 삭제한다.
        - 성공 시 응답 바디는 없다(204).
        """,
    security = @SecurityRequirement(name = "Bearer Token")
  )
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "회원탈퇴 성공, 응답 바디 없음"),
    @ApiResponse(responseCode = "401", description = "Authorization 헤더가 없거나 accessToken이 유효하지 않음"),
    @ApiResponse(responseCode = "404", description = "존재하지 않는 회원")
  })
  ResponseEntity<Void> withdraw(UUID memberId);
}
