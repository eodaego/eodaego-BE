package com.chuseok22.eodaegoserver.domain.course.dto.response;

import com.chuseok22.eodaegoserver.domain.catalog.CatalogCategory;
import com.chuseok22.eodaegoserver.domain.course.entity.CoursePlace;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public record CoursePlaceResponse(

    @Schema(description = "방문 순서(1부터 시작)", example = "1")
    int visitOrder,

    @Schema(description = "AI 서버 기준 시설 ID", example = "11")
    Long facilityId,

    @Schema(description = "연결된 도감 항목 ID. 도감에 동기화되지 않은 시설이면 null", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID catalogItemId,

    @Schema(description = "장소 이름. 도감(catalog_item, category=PLACE)에 동기화된 시설이면 그 이름을, 아직 동기화되지 않았으면 AI가 준 시설 이름을 채운다(둘 다 있으므로 null이 아니다).", example = "맹수마을")
    String name,

    @Schema(description = "장소 카테고리. AI 시설의 원본 category를 변환한 값으로,동물나라는 ANIMAL, 자연나라는 PLANT, 그 외 시설은 PLACE로 반환한다.", example = "PLACE")
    CatalogCategory category,

    @Schema(description = "현재 로그인한 회원의 도감 수집 여부", example = "true")
    boolean collected,

    @Schema(description = "위도. 도감에 동기화된 시설이면 그 값을, 아직 동기화되지 않았으면 AI가 준 좌표를 채운다.", example = "37.5487")
    Double latitude,

    @Schema(description = "경도. 도감에 동기화된 시설이면 그 값을, 아직 동기화되지 않았으면 AI가 준 좌표를 채운다.", example = "127.0812")
    Double longitude,

    @Schema(description = "약도 이미지 기준 X좌표(0.0~1.0). 아직 지원되지 않아 항상 null이다.", example = "null")
    Double mapX,

    @Schema(description = "약도 이미지 기준 Y좌표(0.0~1.0). 아직 지원되지 않아 항상 null이다.", example = "null")
    Double mapY

) {

  public static CoursePlaceResponse from(CoursePlace coursePlace, CoursePlaceCatalogInfo catalogInfo) {
    UUID catalogItemId = catalogInfo != null ? catalogInfo.catalogItemId() : null;
    boolean collected = catalogInfo != null && catalogInfo.collected();

    return new CoursePlaceResponse(
        coursePlace.getVisitOrder(),
        coursePlace.getFacilityId(),
        catalogItemId,
        coursePlace.getName(),
        coursePlace.getCategory(),
        collected,
        coursePlace.getLatitude(),
        coursePlace.getLongitude(),
        coursePlace.getMapX(),
        coursePlace.getMapY()
    );
  }
}
