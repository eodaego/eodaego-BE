package com.chuseok22.eodaegoserver.domain.course.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record CourseFavoriteListResponse(

    @Schema(description = "즐겨찾기한 코스 총 개수. 페이지네이션이 없으므로 항상 items의 길이와 같다.", example = "3")
    int totalCount,

    @Schema(description = "요청한 sort 기준으로 정렬된 즐겨찾기 목록. 즐겨찾기한 코스가 없으면 빈 배열이다.")
    List<CourseFavoriteItemResponse> items

) {

  public static CourseFavoriteListResponse from(List<CourseFavoriteItemResponse> items) {
    return new CourseFavoriteListResponse(items.size(), items);
  }
}
