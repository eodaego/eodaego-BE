package com.chuseok22.eodaegoserver.domain.course.dto.external;

import com.chuseok22.eodaegoserver.domain.catalog.dto.external.AiFacilityResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AiRouteStop(

  AiFacilityResponse facility,

  int order

) {

  public Long facilityId() {
    return facility.id();
  }

  public String facilityCategory() {
    return facility.category();
  }

}
