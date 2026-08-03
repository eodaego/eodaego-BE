package com.chuseok22.eodaegoserver.domain.admin.dto.response;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import java.time.LocalDate;
import java.time.LocalDateTime;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CulturalEventView(
    Integer id,
    String title,
    String place,
    LocalDate startDate,
    LocalDate endDate,
    String description,
    String target,
    String fee,
    String homepageUrl,
    LocalDateTime updatedAt
) {
}
