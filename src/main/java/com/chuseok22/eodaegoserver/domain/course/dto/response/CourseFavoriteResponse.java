package com.chuseok22.eodaegoserver.domain.course.dto.response;

import com.chuseok22.eodaegoserver.domain.course.entity.CourseFavorite;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

public record CourseFavoriteResponse(

    @Schema(description = "즐겨찾기 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID id,

    @Schema(description = "즐겨찾기한 코스 ID", example = "9c858901-8a57-4791-81fe-4c455b099bc9")
    UUID courseId,

    @Schema(description = "즐겨찾기한 시각", example = "2026-07-16T10:30:00")
    LocalDateTime favoritedAt

) {

  public static CourseFavoriteResponse from(CourseFavorite courseFavorite) {
    return new CourseFavoriteResponse(
        courseFavorite.getId(),
        courseFavorite.getCourse().getId(),
        courseFavorite.getCreatedAt()
    );
  }
}
