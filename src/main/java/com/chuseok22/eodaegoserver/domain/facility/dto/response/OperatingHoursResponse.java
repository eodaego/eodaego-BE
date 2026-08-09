package com.chuseok22.eodaegoserver.domain.facility.dto.response;

import com.chuseok22.eodaegoserver.domain.facility.dto.external.AiOperatingHoursResponse;
import io.swagger.v3.oas.annotations.media.Schema;

public record OperatingHoursResponse(

  @Schema(description = "운영시간 안내 제목")
  String sectionTitle,

  @Schema(description = "운영시간 HTML 내용")
  String contentHtml,

  @Schema(description = "노출 순서")
  Integer displayOrder

) {

  public static OperatingHoursResponse from(AiOperatingHoursResponse operatingHours) {
    return new OperatingHoursResponse(
      operatingHours.sectionTitle(),
      operatingHours.contentHtml(),
      operatingHours.displayOrder()
    );
  }
}
