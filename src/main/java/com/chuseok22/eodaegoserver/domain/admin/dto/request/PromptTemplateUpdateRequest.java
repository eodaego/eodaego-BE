package com.chuseok22.eodaegoserver.domain.admin.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PromptTemplateUpdateRequest(
    @NotBlank(message = "템플릿 이름은 필수입니다.") String name,
    @NotBlank(message = "모델은 필수입니다.") String model,
    @NotBlank(message = "용도는 필수입니다.")
    @Pattern(regexp = "^(chat|recommendation)$", message = "용도는 chat 또는 recommendation이어야 합니다.")
    String purpose,
    @NotBlank(message = "템플릿 내용은 필수입니다.") String templateText,
    @JsonProperty("is_active") Boolean active
) {
}
