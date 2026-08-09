package com.chuseok22.eodaegoserver.domain.facility.dto.response;

import com.chuseok22.eodaegoserver.domain.facility.dto.external.AiFacilityResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record FacilityDetailResponse(

  @Schema(description = "AI 서버 시설 ID")
  Long id,

  @Schema(description = "시설 식별 코드")
  String code,

  @Schema(description = "시설 분류")
  String category,

  @Schema(description = "시설 이름")
  String name,

  @Schema(description = "간단 소개")
  String intro,

  @Schema(description = "상세 설명")
  String description,

  @Schema(description = "위도")
  Double latitude,

  @Schema(description = "경도")
  Double longitude,

  @Schema(description = "세부 시설 유형")
  String facilityType,

  @Schema(description = "운영시간 안내")
  List<OperatingHoursResponse> operatingHours

) {

  public static FacilityDetailResponse from(
    AiFacilityResponse facility,
    List<OperatingHoursResponse> operatingHours
  ) {
    return new FacilityDetailResponse(
      facility.id(),
      facility.code(),
      facility.category(),
      facility.name(),
      facility.intro(),
      facility.description(),
      facility.latitude(),
      facility.longitude(),
      facility.facilityType(),
      operatingHours
    );
  }
}