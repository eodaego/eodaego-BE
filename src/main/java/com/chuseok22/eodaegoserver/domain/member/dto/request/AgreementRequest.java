package com.chuseok22.eodaegoserver.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public record AgreementRequest(

    @Schema(description = "개인정보처리방침 동의 여부(필수, true여야함)", example = "true")
    @NotNull @AssertTrue Boolean privacyPolicyAgreed,

    @Schema(description = "위치정보 수집 동의 여부(필수, true여야함)", example = "true")
    @NotNull @AssertTrue Boolean locationInfoAgreed,

    @Schema(description = "이용약관 동의 여부(필수, true여야함)", example = "true")
    @NotNull @AssertTrue Boolean termsOfServiceAgreed,

    @Schema(description = "마케팅 정보 수신 동의 여부(선택)", example = "false")
    boolean marketingAgreed

) {

}
