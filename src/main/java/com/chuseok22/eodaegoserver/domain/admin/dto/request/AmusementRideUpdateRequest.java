package com.chuseok22.eodaegoserver.domain.admin.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AmusementRideUpdateRequest(
    @NotBlank(message = "놀이기구 이름은 필수입니다.") String name,
    String description,
    String location,
    @JsonProperty("is_active") Boolean active
) {
}
