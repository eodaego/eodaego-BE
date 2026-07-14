package com.chuseok22.eodaegoserver.domain.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import java.time.LocalDateTime;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AmusementRideView(
    Integer id,
    String name,
    String description,
    String location,
    @JsonProperty("is_active") boolean active,
    LocalDateTime updatedAt
) {
}
