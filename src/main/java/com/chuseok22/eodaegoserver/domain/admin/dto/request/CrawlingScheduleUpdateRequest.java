package com.chuseok22.eodaegoserver.domain.admin.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CrawlingScheduleUpdateRequest(
    @Pattern(regexp = "^(interval|cron)$", message = "트리거 유형은 interval 또는 cron이어야 합니다.")
    String triggerType,
    @NotBlank(message = "트리거 설정값은 필수입니다.") String triggerConfig,
    @JsonProperty("is_active") Boolean active
) {
}
