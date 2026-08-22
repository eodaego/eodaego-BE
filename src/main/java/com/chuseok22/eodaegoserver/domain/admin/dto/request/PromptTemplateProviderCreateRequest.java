package com.chuseok22.eodaegoserver.domain.admin.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PromptTemplateProviderCreateRequest(
    @NotBlank(message = "provider는 필수입니다.")
    @Pattern(regexp = "^(SUH_AIDER|GEMINI)$", message = "provider는 SUH_AIDER, GEMINI 중 하나여야 합니다.")
    String provider,
    @NotBlank(message = "모델은 필수입니다.") String model,
    @NotNull(message = "priority는 필수입니다.") Integer priority,
    @JsonProperty("is_enabled") boolean enabled
) {
}
