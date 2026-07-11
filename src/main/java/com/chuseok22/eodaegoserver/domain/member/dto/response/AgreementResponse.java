package com.chuseok22.eodaegoserver.domain.member.dto.response;

import com.chuseok22.eodaegoserver.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record AgreementResponse(
    @Schema(description = "개인정보처리방침 동의 여부", example = "true")
    boolean privacyPolicyAgreed,

    @Schema(description = "위치정보 수집 동의 여부", example = "true")
    boolean locationInfoAgreed,

    @Schema(description = "이용약관 동의 여부", example = "true")
    boolean termsOfServiceAgreed,

    @Schema(description = "필수 약관 동의 시각", example = "2026-07-10T12:00:00")
    LocalDateTime termsAgreedAt,

    @Schema(description = "마케팅 정보 수신 동의 여부", example = "false")
    boolean marketingAgreed,

    @Schema(description = "마케팅 정보 수신 동의 시각(동의 안 했으면 null)", example = "null")
    LocalDateTime marketingAgreedAt
) {

  public static AgreementResponse from(Member member) {
    return new AgreementResponse(
        member.isPrivacyPolicyAgreed(),
        member.isLocationInfoAgreed(),
        member.isTermsOfServiceAgreed(),
        member.getTermsAgreedAt(),
        member.isMarketingAgreed(),
        member.getMarketingAgreedAt()
    );
  }
}
