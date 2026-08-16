package com.chuseok22.eodaegoserver.domain.admin.dto.response;

import com.chuseok22.eodaegoserver.domain.member.DeviceType;
import com.chuseok22.eodaegoserver.domain.member.SocialType;
import com.chuseok22.eodaegoserver.domain.member.entity.Member;
import java.time.LocalDateTime;
import java.util.UUID;

public record MemberListView(
    UUID id,
    String nickname,
    String email,
    SocialType socialType,
    DeviceType deviceType,
    LocalDateTime createdAt
) {

  public static MemberListView from(Member member) {
    return new MemberListView(
        member.getId(),
        member.getNickname(),
        member.getEmail(),
        member.getSocialType(),
        member.getDeviceType(),
        member.getCreatedAt()
    );
  }
}
