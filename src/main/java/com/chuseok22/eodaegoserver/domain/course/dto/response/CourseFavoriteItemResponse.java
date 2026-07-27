package com.chuseok22.eodaegoserver.domain.course.dto.response;

import com.chuseok22.eodaegoserver.domain.course.entity.CourseFavorite;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

public record CourseFavoriteItemResponse(

  @Schema(description = "즐겨찾기 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
  UUID favoriteId,

  @Schema(description = "즐겨찾기에 저장한 시각", example = "2026-07-16T10:30:00")
  LocalDateTime savedAt,

  @Schema(description = "즐겨찾기한 코스 정보")
  CourseResponse course

) {

  public static CourseFavoriteItemResponse from(
    CourseFavorite courseFavorite
  ) {
    return new CourseFavoriteItemResponse(
      courseFavorite.getId(),
      courseFavorite.getCreatedAt(),
      CourseResponse.from(courseFavorite.getCourse(), true)
    );
  }
}