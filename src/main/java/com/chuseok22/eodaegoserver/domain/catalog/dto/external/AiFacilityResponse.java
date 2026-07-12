package com.chuseok22.eodaegoserver.domain.catalog.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AiFacilityResponse(

    Long id,             // dedup 키
    String name,         // 이름
    String intro,        // 소개
    String description,  // 설명
    Double latitude,     // 위도
    Double longitude,    // 경도
    @JsonProperty("facility_type")
    String facilityType  // 시설 유형

) {


}
