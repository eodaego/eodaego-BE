package com.chuseok22.eodaegoserver.domain.quiz.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

public record   QuizResponse(

    @Schema(description = "생성된 퀴즈 ID. 정답 제출 API에 그대로 전달한다.", example = "9b7d4c2e-1f0a-4c3b-8e6d-5a4b3c2d1e0f")
    UUID quizId,

    @Schema(description = "3지선다 선택지 목록(항상 3개, 순서 무작위). 정답 정보는 포함하지 않는다.")
    List<QuizChoiceResponse> choices

) {
  public static QuizResponse of(UUID quizId, List<QuizChoiceResponse> choices) {
    return new QuizResponse(quizId, choices);
  }
}
