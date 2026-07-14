package com.chuseok22.eodaegoserver.domain.catalog.dto.response;

import com.chuseok22.eodaegoserver.domain.catalog.CatalogCategory;
import com.chuseok22.eodaegoserver.domain.catalog.CatalogItemStatus;
import com.chuseok22.eodaegoserver.domain.catalog.entity.CatalogItem;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public record CatalogItemResponse(

    @Schema(description = "도감 항목 ID", example = "3f2e1a10-...")
    UUID id,

    @Schema(description = "카테고리", example = "ANIMAL")
    CatalogCategory category,

    @Schema(description = "카테고리 내 발번", example = "1")
    int sequenceNumber,

    @Schema(description = "이름", example = "사자")
    String name,

    @Schema(description = "특징", example = "")
    String feature,

    @Schema(description = "어린이용 설명", example = "")
    String childDescription,

    @Schema(description = "공개 상태. AVAILABLE/SUSPENDED/RETIRED만 허용된다.", example = "AVAILABLE")
    CatalogItemStatus status,

    @Schema(description = "이미지 URL", example = "https://...")
    String imageUrl,

    @Schema(description = "위도(장소 전용)", example = "37.5498")
    Double latitude,

    @Schema(description = "경도(장소 전용)", example = "127.0731")
    Double longitude,

    @Schema(description = "외부 ID", example = "42")
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
