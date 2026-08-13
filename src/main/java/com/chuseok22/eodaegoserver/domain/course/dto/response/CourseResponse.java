package com.chuseok22.eodaegoserver.domain.course.dto.response;

import com.chuseok22.eodaegoserver.domain.course.EntranceGate;
import com.chuseok22.eodaegoserver.domain.course.InterestType;
import com.chuseok22.eodaegoserver.domain.course.entity.Course;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CourseResponse(

    @Schema(description = "코스 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID id,

    @Schema(description = "코스 제목", example = "동물 만나러 가는 길")
    String title,

    @Schema(description = "코스 관심 태그 목록", example = "[\"ANIMAL\", \"NATURE\"]")
    List<InterestType> interestTypes,

    @Schema(description = "코스 특징을 요약하는 짧은 태그 목록. AI가 코스마다 1~3개 생성한다.", example = "[\"동물듬뿍\", \"산책하기 좋은 코스\"]")
    List<String> tagLabels,

    @Schema(description = "AI가 계산한 코스 완주 예상 소요시간(분). 요청의 stayDurationMinutes(희망 체류시간)와는 다른 값이다.", example = "120")
    int estimatedDurationMinutes,

    @Schema(description = "입구", example = "MAIN_GATE")
    EntranceGate entrance,

    @Schema(description = "출구", example = "SOUTH_GATE")
    EntranceGate exit,

    @Schema(description = "현재 조회하는 회원이 이 코스를 즐겨찾기했는지 여부", example = "false")
    boolean favorite,

    @Schema(description = "방문 순서대로 정렬된 장소 목록", example = """
        [
          {"visitOrder": 1, "facilityId": 11, "catalogItemId": "3fa85f64-5717-4562-b3fc-2c963f66afa6", "name": "맹수마을", "category": "ANIMAL", "collected": true, "latitude": 37.5487, "longitude": 127.0812, "mapX": null, "mapY": null},
          {"visitOrder": 2, "facilityId": 12, "catalogItemId": null, "name": "바다동물관", "category": "PLACE", "collected": false, "latitude": 37.5491, "longitude": 127.0825, "mapX": null, "mapY": null}
        ]
        """)
    List<CoursePlaceResponse> places

) {

  public static CourseResponse from(
      Course course,
      boolean favorite,
      Map<Long, CoursePlaceCatalogInfo> catalogInfoByFacilityId
  ) {
    return new CourseResponse(
        course.getId(),
        course.getTitle(),
        List.copyOf(course.getInterestTypes()),
        List.copyOf(course.getTagLabels()),
        course.getEstimatedDurationMinutes(),
        course.getEntrance(),
        course.getExit(),
        favorite,
        course.getPlaces().stream()
            .map(place -> CoursePlaceResponse.from(place, catalogInfoByFacilityId.get(place.getFacilityId())))
            .toList()
    );
  }
}
