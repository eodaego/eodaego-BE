package com.chuseok22.eodaegoserver.domain.auth.dto.request;

import com.chuseok22.eodaegoserver.domain.member.DeviceType;
import com.chuseok22.eodaegoserver.domain.member.SocialType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
    @Schema(description = "Firebase Admin SDK로 검증할 소셜 로그인 ID Token", example = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjA5N...")
    @NotBlank String idToken,

    @Schema(description = "소셜 로그인 제공자. GOOGLE 또는 APPLE만 허용된다.", example = "GOOGLE")
    @NotNull SocialType socialType,

    @Schema(description = "로그인한 디바이스 종류. IOS 또는 ANDROID만 허용된다.", example = "IOS")
    @NotNull DeviceType deviceType,

    @Schema(description = "디바이스 고유 식별자", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    @NotBlank String deviceId,

    @Schema(description = "푸시 알림용 FCM 토큰(선택, 미보유 시 생략 가능)", example = "dXJ2c2FtcGxlZmNtdG9rZW4...")
    String fcmToken
) {

}
