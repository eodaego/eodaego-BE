package com.chuseok22.eodaegoserver.domain.course.dto.response;

import com.chuseok22.eodaegoserver.domain.catalog.CatalogCategory;
import com.chuseok22.eodaegoserver.domain.course.entity.CoursePlace;
import io.swagger.v3.oas.annotations.media.Schema;

public record CoursePlaceResponse(

    @Schema(description = "방문 순서(1부터 시작)", example = "1")
    int visitOrder,

    @Schema(description = "AI 서버 기준 시설 ID", example = "42")
    Long facilityId,

    @Schema(description = "장소 이름", example = "동물나라")
    String name,

    @Schema(description = "카테고리. ANIMAL/PLANT/PLACE만 허용된다.", example = "ANIMAL")
    CatalogCategory category,

    @Schema(description = "위도", example = "37.5498")
    Double latitude,

    @Schema(description = "경도", example = "127.0731")
    Double longitude,

    @Schema(description = "약도 이미지 기준 X좌표(0.0~1.0). 아직 지원되지 않아 항상 null이다.", example = "null")
    Double mapX,

    @Schema(description = "약도 이미지 기준 Y좌표(0.0~1.0). 아직 지원되지 않아 항상 null이다.", example = "null")
    Double mapY

) {

  public static CoursePlaceResponse from(CoursePlace coursePlace) {
    return new CoursePlaceResponse(
        coursePlace.getVisitOrder(),
        coursePlace.getFacilityId(),
        coursePlace.getName(),
        coursePlace.getCategory(),
        coursePlace.getLatitude(),
        coursePlace.getLongitude(),
        coursePlace.getMapX(),
        coursePlace.getMapY()
    );
  }
}
