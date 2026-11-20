package com.chuseok22.eodaegoserver.domain.admin.dto.request;

import com.chuseok22.eodaegoserver.domain.member.DeviceType;
import com.chuseok22.eodaegoserver.domain.member.SocialType;

public record MemberListQuery(
    String keyword,
    SocialType socialType,
    DeviceType deviceType,
    String sort,
    String direction,
    int page
) {
}
