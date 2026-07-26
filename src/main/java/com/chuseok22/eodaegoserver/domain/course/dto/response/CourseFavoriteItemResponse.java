package com.chuseok22.eodaegoserver.domain.course.dto.response;

import com.chuseok22.eodaegoserver.domain.course.entity.CourseFavorite;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record CourseFavoriteItemResponse(

    @Schema(description = "즐겨찾기한 시각", example = "2026-07-16T10:30:00")
    LocalDateTime favoritedAt,

    @Schema(description = "즐겨찾기한 코스 정보")
    CourseResponse course

) {

  public static CourseFavoriteItemResponse from(CourseFavorite courseFavorite) {
    return new CourseFavoriteItemResponse(
        courseFavorite.getCreatedAt(),
        CourseResponse.from(courseFavorite.getCourse(), true)
    );
  }

}
