package com.chuseok22.eodaegoserver.domain.facility.dto.response;

import com.chuseok22.eodaegoserver.domain.facility.dto.external.AiFacilityResponse;
import io.swagger.v3.oas.annotations.media.Schema;

public record FacilitySummaryResponse(

  @Schema(description = "AI 서버 시설 ID", example = "1")
  Long id,

  @Schema(description = "시설 식별 코드", example = "MAIN_GATE")
  String code,

  @Schema(description = "시설 분류", example = "출입문")
  String category,

  @Schema(description = "시설 이름", example = "정문")
  String name,

  @Schema(description = "위도", example = "37.5498")
  Double latitude,

  @Schema(description = "경도", example = "127.0731")
  Double longitude,

  @Schema(description = "세부 시설 유형", example = "안내")
  String facilityType

) {

  public static FacilitySummaryResponse from(AiFacilityResponse facility) {
    return new FacilitySummaryResponse(
      facility.id(),
      facility.code(),
      facility.category(),
      facility.name(),
      facility.latitude(),
      facility.longitude(),
      facility.facilityType()
    );
  }
}
