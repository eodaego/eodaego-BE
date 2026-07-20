package com.chuseok22.eodaegoserver.domain.admin.dto.response;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record WeatherSnapshotView(
    Integer id,
    String placeRefKey,
    Double temperature,
    Integer humidity,
    String precipitationType,
    Double windSpeed,
    String skyCondition,
    List<Map<String, Object>> hourlyForecast,
    LocalDateTime observedAt,
    LocalDateTime collectedAt
) {
}
