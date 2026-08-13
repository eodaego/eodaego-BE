package com.chuseok22.eodaegoserver.domain.facility.dto.response;

import com.chuseok22.eodaegoserver.domain.facility.entity.Facility;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalTime;

public record FacilityDetailResponse(

    @Schema(description = "시설 ID. AI 서버가 매긴 번호이며 코스 응답의 facilityId, 도감 응답의 externalId와 같은 값이다.", example = "12")
    Long id,

    @Schema(description = "시설 분류.", example = "편의시설")
    String category,

    @Schema(description = "시설 이름.", example = "꿈마루")
    String name,

    @Schema(description = "간단 소개. 없으면 null이다.", example = "옛 골프장 클럽하우스를 리모델링한 복합문화공간이다.")
    String intro,

    @Schema(description = "상세 설명. 없으면 null이다.", example = "1970년대 지어진 건물의 골조를 남기고 재생한 공간으로 전시와 휴게 공간을 갖췄다.")
    String description,

    @Schema(description = "위도. 좌표 정보가 없는 시설은 null이다.", example = "37.5501")
    Double latitude,

    @Schema(description = "경도. 좌표 정보가 없는 시설은 null이다.", example = "127.0798")
    Double longitude,

    @Schema(description = "세부 시설 유형. 출입문 등 유형이 없는 시설은 null이다.", example = "문화")
    String facilityType,

    @JsonFormat(pattern = "HH:mm")
    @Schema(description = "개장 시각(HH:mm). 상시 개방이거나 운영시간 정보가 없으면 null이다.", example = "10:00")
    LocalTime openTime,

    @JsonFormat(pattern = "HH:mm")
    @Schema(description = "폐장 시각(HH:mm). 상시 개방이거나 운영시간 정보가 없으면 null이다.", example = "17:00")
    LocalTime closeTime,

    @Schema(description = "시간으로 표현할 수 없는 운영 안내. 없으면 null이다.", example = "매주 월요일 휴관")
    String operatingNote

) {

  public static FacilityDetailResponse from(Facility facility) {
    return new FacilityDetailResponse(
        facility.getAiFacilityId(),
        facility.getSourceCategory(),
        facility.getName(),
        facility.getIntro(),
        facility.getDescription(),
        facility.getLatitude(),
        facility.getLongitude(),
        facility.getFacilityType(),
        facility.getOpenTime(),
        facility.getCloseTime(),
        facility.getOperatingNote()
    );
  }
}
