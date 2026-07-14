package com.chuseok22.eodaegoserver.domain.admin.dto.response;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import java.time.LocalDateTime;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PlantView(
    Integer id,
    Integer msgSeq,
    String category,
    String name,
    String description,
    String registeredDate,
    String thumbnailUrl,
    String sourceUrl,
    LocalDateTime updatedAt
) {
}
