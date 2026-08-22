package com.chuseok22.eodaegoserver.domain.admin.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Pattern;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PromptTemplateProviderUpdateRequest(
    @Pattern(regexp = "^(SUH_AIDER|GEMINI)$", message = "provider는 SUH_AIDER, GEMINI 중 하나여야 합니다.")
    String provider,
    String model,
    Integer priority,
    @JsonProperty("is_enabled") Boolean enabled
) {
}
