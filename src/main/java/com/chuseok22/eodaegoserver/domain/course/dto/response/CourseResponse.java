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

    @Schema(description = "코스 대표 관심 태그", example = "ANIMAL")
    InterestType interestType,

    @Schema(description = "추천 이유/태그 문구", example = "동물을 좋아하는 분께 추천하는 코스예요")
    String tagLabel,

    @Schema(description = "소요 시간(분)", example = "120")
    int durationMinutes,

    @Schema(description = "입구", example = "MAIN_GATE")
    EntranceGate entrance,

    @Schema(description = "출구", example = "SOUTH_GATE")
    EntranceGate exit,

    @Schema(description = "현재 조회하는 회원이 이 코스를 즐겨찾기했는지 여부", example = "false")
    boolean favorite,

    @Schema(description = "방문 순서대로 정렬된 장소 목록")
    List<CoursePlaceResponse> places

) {

  public static CourseResponse from(Course course, boolean favorite) {
    return new CourseResponse(
        course.getId(),
        course.getTitle(),
        course.getInterestType(),
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
