package com.chuseok22.eodaegoserver.domain.facility.dto.response;

import com.chuseok22.eodaegoserver.domain.facility.entity.Facility;
import io.swagger.v3.oas.annotations.media.Schema;

public record FacilitySummaryResponse(

    @Schema(description = "시설 ID. AI 서버가 매긴 번호이며 코스 응답의 facilityId, 도감 응답의 externalId와 같은 값이다.", example = "12")
    Long id,

    @Schema(description = "시설 분류.", example = "편의시설")
    String category,

    @Schema(description = "시설 이름.", example = "꿈마루")
    String name,

    @Schema(description = "위도. 좌표 정보가 없는 시설은 null이므로 지도 마커를 그리기 전에 확인해야 한다.", example = "37.5501")
    Double latitude,

    @Schema(description = "경도. 좌표 정보가 없는 시설은 null이다.", example = "127.0798")
    Double longitude,

    @Schema(description = "세부 시설 유형. 출입문 등 유형이 없는 시설은 null이다.", example = "문화")
    String facilityType

) {

  public static FacilitySummaryResponse from(Facility facility) {
    return new FacilitySummaryResponse(
        facility.getAiFacilityId(),
        facility.getSourceCategory(),
        facility.getName(),
        facility.getLatitude(),
        facility.getLongitude(),
        facility.getFacilityType()
    );
  }
}
