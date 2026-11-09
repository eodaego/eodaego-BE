package com.chuseok22.eodaegoserver.domain.quiz.dto.response;

import com.chuseok22.eodaegoserver.domain.catalog.entity.CatalogItem;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public record QuizChoiceResponse(

    @Schema(description = "선택지 도감 항목 ID. 정답 제출 시 이 값을 selectedCatalogItemId로 그대로 보낸다.", example = "3f2e1a10-0c11-4b7e-9a1e-2b3c4d5e6f70")
    UUID catalogItemId,

    @Schema(description = "선택지 이름", example = "사자")
    String name

) {
  public static QuizChoiceResponse from(CatalogItem catalogItem) {
    return new QuizChoiceResponse(catalogItem.getId(), catalogItem.getName());
  }
}
