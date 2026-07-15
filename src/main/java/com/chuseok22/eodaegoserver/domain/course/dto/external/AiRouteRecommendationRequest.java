package com.chuseok22.eodaegoserver.domain.course.dto.external;

import com.chuseok22.eodaegoserver.domain.course.CompanionType;
import com.chuseok22.eodaegoserver.domain.course.EntranceGate;
import com.chuseok22.eodaegoserver.domain.course.InterestType;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record AiRouteRecommendationRequest(

    @JsonProperty("preference_tags")
    List<InterestType> preferenceTags,       // 선호 태그(최소 1개)

    @JsonProperty("stay_duration_minutes")
    int stayDurationMinutes,                 // 체류 시간(분)

    @JsonProperty("entrance_facility_code")
    EntranceGate entranceFacilityCode,       // 입구 코드

    @JsonProperty("exit_facility_code")
    EntranceGate exitFacilityCode,           // 출구 코드

    @JsonProperty("companion_type")
    CompanionType companionType

) {

}
