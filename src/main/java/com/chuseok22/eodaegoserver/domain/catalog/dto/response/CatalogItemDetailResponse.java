package com.chuseok22.eodaegoserver.domain.catalog.dto.response;

import com.chuseok22.eodaegoserver.domain.catalog.CatalogCategory;
import com.chuseok22.eodaegoserver.domain.catalog.CatalogItemStatus;
import com.chuseok22.eodaegoserver.domain.catalog.entity.CatalogItem;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

public record CatalogItemDetailResponse(

    @Schema(description = "도감 항목 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID id,

    @Schema(description = "카테고리별 식별 코드(카테고리 접두사 + 발번).", example = "A001")
    String code,

    @Schema(description = "이름", example = "다람쥐")
    String name,

    @Schema(description = "카테고리. ANIMAL(동물), PLANT(식물), PLACE(장소) 중 하나.", example = "ANIMAL")
    CatalogCategory category,

    @Schema(description = "특징 설명", example = "몸길이 20cm 내외의 작은 포유류로, 나무를 잘 탄다.")
    String feature,

    @Schema(description = "어린이 눈높이 설명", example = "다람쥐는 도토리를 좋아하는 작은 친구예요!")
    String childDescription,

    @Schema(description = "이미지 URL", example = "https://cdn.eodaego.com/animals/squirrel.png")
    String imageUrl,

    @Schema(description = "수집 일시(KST, yyyy-MM-dd'T'HH:mm:ss)", example = "2026-07-01T14:30:00")
    LocalDateTime collectedAt,

    @Schema(description = "수집 가능 상태. AVAILABLE(수집 가능), SUSPENDED(임시 중단), RETIRED(운영 종료) 중 하나.", example = "AVAILABLE")
    CatalogItemStatus status

) {
  public static CatalogItemDetailResponse from(CatalogItem catalogItem, String code, LocalDateTime collectedAt) {
    return new CatalogItemDetailResponse(
        catalogItem.getId(),
        code,
        catalogItem.getName(),
        catalogItem.getCategory(),
        catalogItem.getFeature(),
        catalogItem.getChildDescription(),
        catalogItem.getImageUrl(),
        collectedAt,
        catalogItem.getStatus()
    );
  }
}
