package com.chuseok22.eodaegoserver.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record ReissueResponse(
    @Schema(description = "API 요청 시 Authorization: Bearer {accessToken} 헤더에 사용하는 액세스 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
    String accessToken,

    @Schema(description = "accessToken 만료 시 재발급에 사용하는 리프레시 토큰. 회원당 1개만 유지되며 재발급마다 새로 발급된다(Rotation).", example = "eyJhbGciOiJIUzI1NiJ9...")
    String refreshToken
) {

}
