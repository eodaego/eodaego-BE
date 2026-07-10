package com.chuseok22.eodaegoserver.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public record LoginResponse(

    @Schema(description = "API 요청 시 Authorization: Bearer {accessToken} 헤더에 사용하는 액세스 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
    String accessToken,

    @Schema(description = "accessToken 만료 시 재발급에 사용하는 리프레시 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
    String refreshToken,

    @Schema(description = "이번 요청으로 신규 회원가입이 함께 처리되었는지 여부. true면 최초 로그인(자동 회원가입)이라는 뜻이다.", example = "false")
    boolean firstLogin,

    @Schema(description = "필수 약관(개인정보처리방침/위치정보/이용약관) 중 하나라도 미동의 상태인지 여부. true면 클라이언트가 약관 동의 화면을 띄우고 PATCH /members/me/agreements를 호출해야 한다 .", example = "true")
    boolean requiresAgreement,

    @Schema(description = "회원 닉네임", example = "회원a1b2c3d4")
    String nickname,

    @Schema(description = "회원 고유 ID(PK)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID userId
) {

}
