package com.chuseok22.eodaegoserver.domain.admin.dto.request;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PreferenceCategoryMappingCreateRequest(
    @NotBlank(message = "취향 태그는 필수입니다.")
    @Pattern(
        regexp = "^(ANIMAL|NATURE|ACTIVITY|PHOTO_SPOT|RELAXATION|CULTURE_EVENT|LEARNING)$",
        message = "취향 태그는 ANIMAL, NATURE, ACTIVITY, PHOTO_SPOT, RELAXATION, CULTURE_EVENT, LEARNING 중 하나여야 합니다."
    )
    String preferenceTag,
    @NotBlank(message = "카테고리는 필수입니다.")
    @Size(max = 50, message = "카테고리는 50자 이하여야 합니다.")
    String category
) {
}
