package com.chuseok22.eodaegoserver.domain.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PromptTemplateProviderView(
    Integer id,
    String provider,
    String model,
    Integer priority,
    @JsonProperty("is_enabled") boolean enabled
) {
}
