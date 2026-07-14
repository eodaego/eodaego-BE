package com.chuseok22.eodaegoserver.domain.admin.dto.response;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import java.time.LocalDateTime;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AnimalView(
    Integer id,
    Integer msgSeq,
    String category,
    String name,
    String scientificName,
    String englishName,
    String classification,
    String distribution,
    String diet,
    String registeredDate,
    String thumbnailUrl,
    String locationName,
    String sourceUrl,
    LocalDateTime updatedAt
) {
}
