package com.chuseok22.eodaegoserver.domain.catalog.dto.response;

import com.chuseok22.eodaegoserver.domain.catalog.CatalogCategory;
import com.chuseok22.eodaegoserver.domain.catalog.CatalogItemStatus;
import com.chuseok22.eodaegoserver.domain.catalog.entity.CatalogItem;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public record CatalogItemResponse(

    @Schema(description = "도감 항목 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID id,

    @Schema(description = "카테고리. ANIMAL(동물), PLANT(식물), PLACE(장소) 중 하나.", example = "ANIMAL")
    CatalogCategory category,

    @Schema(description = "카테고리 내 발번", example = "1")
    int sequenceNumber,

    @Schema(description = "이름. 관리자가 지정한 값이 있으면 그 값, 없으면 AI 원본 이름.", example = "사자")
    String name,

    @Schema(description = "특징 설명. 관리자가 지정한 값이 있으면 그 값, 없으면 AI 원본 설명(없으면 빈 문자열).",
        example = "무리를 지어 사는 대형 고양잇과 동물이다.")
    String feature,

    @Schema(description = "어린이 눈높이 설명. AI가 제공하지 않아 관리자가 직접 작성한다.",
        example = "사자는 갈기가 멋진 초원의 왕이에요!")
    String childDescription,

    @Schema(description = "공개 상태. AVAILABLE(수집 가능), SUSPENDED(임시 중단), RETIRED(운영 종료) 중 하나.",
        example = "AVAILABLE")
    CatalogItemStatus status,

    @Schema(description = "이미지 URL. 관리자가 지정한 값이 있으면 그 값, 없으면 AI 원본 이미지.",
        example = "https://cdn.eodaego.com/animals/lion.png")
    String imageUrl,

    @Schema(description = "위도. 장소(PLACE) 전용. 관리자가 지정한 값이 있으면 그 값, 없으면 AI 원본 좌표.",
        example = "37.5498")
    Double latitude,

    @Schema(description = "경도. 장소(PLACE) 전용. 관리자가 지정한 값이 있으면 그 값, 없으면 AI 원본 좌표.",
        example = "127.0731")
    Double longitude,

    @Schema(description = "AI 서버 기준 원본 ID", example = "42")
    Long externalId

) {

  public static CatalogItemResponse from(CatalogItem catalogItem) {
    return new CatalogItemResponse(
        catalogItem.getId(),
        catalogItem.getCategory(),
        catalogItem.getSequenceNumber(),
        catalogItem.getName(),
        catalogItem.getFeature(),
        catalogItem.getChildDescription(),
        catalogItem.getStatus(),
        catalogItem.getImageUrl(),
        catalogItem.getLatitude(),
        catalogItem.getLongitude(),
        catalogItem.getExternalId()
    );
  }
}
