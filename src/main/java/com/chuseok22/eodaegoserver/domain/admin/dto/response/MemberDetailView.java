package com.chuseok22.eodaegoserver.domain.admin.dto.response;

import com.chuseok22.eodaegoserver.domain.member.DeviceType;
import com.chuseok22.eodaegoserver.domain.member.SocialType;
import com.chuseok22.eodaegoserver.domain.member.entity.Member;
import java.time.LocalDateTime;
import java.util.UUID;

public record MemberDetailView(
    UUID id,
    String nickname,
    String email,
    SocialType socialType,
    DeviceType deviceType,
    LocalDateTime createdAt,
    boolean privacyPolicyAgreed,
    boolean locationInfoAgreed,
    boolean termsOfServiceAgreed,
    LocalDateTime termsAgreedAt,
    boolean marketingAgreed,
    LocalDateTime marketingAgreedAt
) {

  public static MemberDetailView from(Member member) {
    return new MemberDetailView(
        member.getId(),
        member.getNickname(),
        member.getEmail(),
        member.getSocialType(),
        member.getDeviceType(),
        member.getCreatedAt(),
        member.isPrivacyPolicyAgreed(),
        member.isLocationInfoAgreed(),
        member.isTermsOfServiceAgreed(),
        member.getTermsAgreedAt(),
        member.isMarketingAgreed(),
        member.getMarketingAgreedAt()
    );
  }
}
