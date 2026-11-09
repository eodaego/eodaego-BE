package com.chuseok22.eodaegoserver.domain.quiz.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public record QuizAnswerResponse(

    @Schema(description = "정답 여부. 오답도 에러가 아닌 정상 응답으로 내려간다(false). 오답이면 그 선택지만 비활성화하고 재시도한다.", example = "true")
    boolean correct,

    @Schema(description = "정답일 때 수집된 도감 항목 ID. 오답이면 null. FE는 이 값으로 도감 상세로 이동한다.", example = "3f2e1a10-0c11-4b7e-9a1e-2b3c4d5e6f70")
    UUID collectedCatalogItemId

) {
  public static QuizAnswerResponse wrong() {
    return new QuizAnswerResponse(false, null);
  }

  public static QuizAnswerResponse correct(UUID collectedCatalogItemId) {
    return new QuizAnswerResponse(true, collectedCatalogItemId);
  }
}
