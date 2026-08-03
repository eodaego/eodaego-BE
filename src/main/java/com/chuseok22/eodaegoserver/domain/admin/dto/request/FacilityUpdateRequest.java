package com.chuseok22.eodaegoserver.domain.admin.dto.request;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record FacilityUpdateRequest(
    @NotBlank(message = "시설 분류는 필수입니다.") String category,
    @NotBlank(message = "시설 이름은 필수입니다.") String name,
    String code,
    String intro,
    String description,
    Double latitude,
    Double longitude,
    String facilityType
) {
  // FacilityCreateRequest와 동일한 이유로 code의 빈 문자열을 null로 정규화한다.
  public FacilityUpdateRequest {
    code = (code == null || code.isBlank()) ? null : code;
  }
}
