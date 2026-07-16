package com.chuseok22.eodaegoserver.domain.admin.dto.response;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import java.time.LocalDateTime;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record FacilityView(
    Integer id,
    Integer externalId,
    String category,
    String name,
    String intro,
    String description,
    Double latitude,
    Double longitude,
    String facilityType,
    LocalDateTime updatedAt
) {
}
