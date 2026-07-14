package com.chuseok22.eodaegoserver.domain.admin.dto.response;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import java.time.LocalDateTime;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record OperatingHoursView(
    Integer id,
    String sectionTitle,
    String contentHtml,
    Integer displayOrder,
    LocalDateTime collectedAt
) {
}
