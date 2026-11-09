package com.chuseok22.eodaegoserver.domain.quiz.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record QuizAnswerRequest(

    @Schema(description = "선택한 선택지의 도감 항목 ID(선택지의 catalogItemId)", example = "3f2e1a10-0c11-4b7e-9a1e-2b3c4d5e6f70")
    @NotNull(message = "selectedCatalogItemId는 필수입니다.")
    UUID selectedCatalogItemId

) {
}
