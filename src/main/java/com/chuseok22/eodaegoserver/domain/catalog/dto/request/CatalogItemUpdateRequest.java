package com.chuseok22.eodaegoserver.domain.catalog.dto.request;

import com.chuseok22.eodaegoserver.domain.catalog.CatalogItemStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CatalogItemUpdateRequest(

    @Schema(description = "AI 원본 이름 대신 사용할 이름. null이면 AI 원본 이름을 그대로 쓴다(원복). "
        + "앞뒤 공백은 제거되며, 도감 항목은 이름이 반드시 있어야 하므로 공백만으로는 채울 수 없다.",
        example = "사자")
    @Pattern(regexp = "(?s).*\\S.*", message = "이름은 공백만으로 채울 수 없습니다.")
    String nameOverride,

    @Schema(description = "AI 원본 설명 대신 사용할 특징 설명. null이면 AI 원본 설명을 그대로 쓰고(원복), "
        + "빈 문자열(\"\")을 보내면 설명 없음으로 고정한다(AI 설명이 부적절할 때 사용). 앞뒤 공백은 제거된다.",
        example = "무리를 지어 사는 대형 고양잇과 동물이다.")
    String featureOverride,

    @Schema(description = "어린이 눈높이 설명. AI가 제공하지 않는 값이라 관리자가 직접 작성한다.",
        example = "사자는 갈기가 멋진 초원의 왕이에요!")
    @NotBlank
    String childDescription,

    @Schema(description = "공개 상태. AVAILABLE(수집 가능), SUSPENDED(임시 중단), RETIRED(운영 종료)만 허용된다.",
        example = "AVAILABLE")
    @NotNull
    CatalogItemStatus status,

    @Schema(description = "AI 원본 이미지 대신 사용할 이미지 URL. null이면 AI 원본 이미지를 그대로 쓰고(원복), "
        + "빈 문자열(\"\")을 보내면 이미지 없음으로 고정해 응답의 imageUrl이 null로 내려간다"
        + "(AI 썸네일이 깨졌을 때 사용). 앞뒤 공백은 제거된다.",
        example = "https://cdn.eodaego.com/animals/lion.png")
    String imageUrlOverride,

    @Schema(description = "AI 원본 위도 대신 사용할 위도. 장소(PLACE) 전용이며 null이면 AI 원본 좌표를 그대로 쓴다(원복).",
        example = "37.5498")
    Double latitudeOverride,

    @Schema(description = "AI 원본 경도 대신 사용할 경도. 장소(PLACE) 전용이며 null이면 AI 원본 좌표를 그대로 쓴다(원복).",
        example = "127.0731")
    Double longitudeOverride

) {

  public CatalogItemUpdateRequest {
    nameOverride = normalize(nameOverride);
    featureOverride = normalize(featureOverride);
    imageUrlOverride = normalize(imageUrlOverride);
  }

  private static String normalize(String value) {
    return value == null ? null : value.strip();
  }
}
