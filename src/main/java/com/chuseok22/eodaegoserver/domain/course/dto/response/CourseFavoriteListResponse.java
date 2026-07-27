package com.chuseok22.eodaegoserver.domain.course.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record CourseFavoriteListResponse(

  @Schema(description = "저장한 코스 개수", example = "3")
  int totalCount,

  @Schema(description = "저장한 코스 목록")
  List<CourseFavoriteItemResponse> items

) {

  public static CourseFavoriteListResponse from(
    List<CourseFavoriteItemResponse> items
  ) {
    return new CourseFavoriteListResponse(
      items.size(),
      items
    );
  }
}