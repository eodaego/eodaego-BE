package com.chuseok22.eodaegoserver.domain.course.dto.response;

import com.chuseok22.eodaegoserver.domain.course.EntranceGate;
import com.chuseok22.eodaegoserver.domain.course.InterestType;
import com.chuseok22.eodaegoserver.domain.course.entity.Course;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

public record CourseResponse(

    @Schema(description = "코스 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID id,

    @Schema(description = "코스 제목", example = "동물 만나러 가는 길")
    String title,

    @Schema(description = "코스 관심 태그 목록", example = "[\"ANIMAL\", \"NATURE\"]")
    List<InterestType> interestTypes,

    @Schema(description = "코스 카드에 붙는 짧은 태그 문구. 현재 구현에서는 항상 null이다.", example = "동물 듬뿍")
    String tagLabel,

    @Schema(description = "소요 시간(분)", example = "120")
    int durationMinutes,

    @Schema(description = "입구", example = "MAIN_GATE")
    EntranceGate entrance,

    @Schema(description = "출구", example = "SOUTH_GATE")
    EntranceGate exit,

    @Schema(description = "현재 조회하는 회원이 이 코스를 즐겨찾기했는지 여부", example = "false")
    boolean favorite,

    @Schema(description = "방문 순서대로 정렬된 장소 목록", example = """
        [
          {"visitOrder": 1, "facilityId": 11, "name": "맹수마을", "category": "ANIMAL", "latitude": 37.5487, "longitude": 127.0812, "mapX": null, "mapY": null},
          {"visitOrder": 2, "facilityId": 12, "name": "바다동물관", "category": "PLACE", "latitude": 37.5491, "longitude": 127.0825, "mapX": null, "mapY": null}
        ]
        """)
    List<CoursePlaceResponse> places

) {

  public static CourseResponse from(Course course, boolean favorite) {
    return new CourseResponse(
        course.getId(),
        course.getTitle(),
        course.getInterestTypes(),
        course.getTagLabel(),
        course.getDurationMinutes(),
        course.getEntrance(),
        course.getExit(),
        favorite,
        course.getPlaces().stream()
            .map(CoursePlaceResponse::from)
            .toList()
    );
  }
}
