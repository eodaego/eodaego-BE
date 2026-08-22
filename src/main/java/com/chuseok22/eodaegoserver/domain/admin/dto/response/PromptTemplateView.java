package com.chuseok22.eodaegoserver.domain.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import java.time.LocalDateTime;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PromptTemplateView(
    Integer id,
    String name,
    String purpose,
    String templateText,
    @JsonProperty("is_active") boolean active,
    List<PromptTemplateProviderView> providers,
    LocalDateTime updatedAt
) {
}
