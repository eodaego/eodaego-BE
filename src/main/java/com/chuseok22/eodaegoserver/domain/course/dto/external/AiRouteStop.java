package com.chuseok22.eodaegoserver.domain.course.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AiRouteStop(

    @JsonProperty("facility_id")
    Long facilityId,

    int order

) {

}
