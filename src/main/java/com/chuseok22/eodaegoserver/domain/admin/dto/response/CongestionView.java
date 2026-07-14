package com.chuseok22.eodaegoserver.domain.admin.dto.response;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CongestionView(
    Integer id,
    String placeRefKey,
    String congestionLevel,
    String congestionMessage,
    Integer populationMin,
    Integer populationMax,
    List<Map<String, Object>> forecast,
    LocalDateTime collectedAt
) {
}
