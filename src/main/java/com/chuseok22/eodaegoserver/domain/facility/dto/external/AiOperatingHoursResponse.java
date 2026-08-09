package com.chuseok22.eodaegoserver.domain.facility.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AiOperatingHoursResponse(

  Integer id,

  @JsonProperty("section_title")
  String sectionTitle,

  @JsonProperty("content_html")
  String contentHtml,

  @JsonProperty("display_order")
  Integer displayOrder

) {

}
