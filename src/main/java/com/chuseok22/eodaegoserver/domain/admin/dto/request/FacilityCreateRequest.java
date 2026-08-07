package com.chuseok22.eodaegoserver.domain.admin.dto.request;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record FacilityCreateRequest(
    @NotBlank(message = "시설 분류는 필수입니다.") String category,
    @NotBlank(message = "시설 이름은 필수입니다.") String name,
    String code,
    String intro,
    String description,
    Double latitude,
    Double longitude,
    String facilityType
) {
  // Thymeleaf 폼은 빈 입력을 null이 아닌 ""로 바인딩한다. code는 AI 서버에서 unique 제약이 있어
  // ""가 그대로 전송되면 두 번째 무-code 시설 등록 시 중복 충돌(409)이 발생하므로 null로 정규화한다.
  public FacilityCreateRequest {
    code = (code == null || code.isBlank()) ? null : code;
  }
}
