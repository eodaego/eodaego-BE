package com.chuseok22.eodaegoserver.domain.course.dto.response;

import com.chuseok22.eodaegoserver.domain.catalog.CatalogCategory;
import com.chuseok22.eodaegoserver.domain.course.entity.CoursePlace;
import io.swagger.v3.oas.annotations.media.Schema;

public record CoursePlaceResponse(

    @Schema(description = "방문 순서(1부터 시작)", example = "1")
    int visitOrder,

    @Schema(description = "AI 서버 기준 시설 ID", example = "11")
    Long facilityId,

    @Schema(description = "장소 이름. 도감(catalog_item, category=PLACE)에 동기화된 시설이면 그 이름이 채워지고, 아직 동기화되지 않은 시설이면 null이다.", example = "맹수마을")
    String name,

    @Schema(description = "카테고리. 도감 매칭에 성공하면 항상 PLACE이고, 매칭에 실패하면 null이다(ANIMAL/PLANT는 나오지 않는다).", example = "PLACE")
    CatalogCategory category,

    @Schema(description = "위도. 도감에 동기화된 시설이면 그 값이 채워지고, 아직 동기화되지 않았으면 null이다.", example = "37.5487")
    Double latitude,

    @Schema(description = "경도. 도감에 동기화된 시설이면 그 값이 채워지고, 아직 동기화되지 않았으면 null이다.", example = "127.0812")
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
