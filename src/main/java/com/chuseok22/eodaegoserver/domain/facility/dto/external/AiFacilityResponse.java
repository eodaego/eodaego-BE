package com.chuseok22.eodaegoserver.domain.facility.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AiFacilityResponse(

  Long id,

  String category,

  String name,

  String code,

  String intro,

  String description,

  Double latitude,

  Double longitude,

  @JsonProperty("facility_type")
  String facilityType

) {

}