package com.chuseok22.eodaegoserver.domain.catalog.dto.response;

import com.chuseok22.eodaegoserver.domain.catalog.CatalogCategory;
import com.chuseok22.eodaegoserver.domain.catalog.CatalogItemStatus;
import com.chuseok22.eodaegoserver.domain.catalog.entity.CatalogItem;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public record CatalogItemSummaryResponse(

    @Schema(description = "도감 항목 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID id,

    @Schema(description = "카테고리별 식별 코드(카테고리 접두사 + 발번).", example = "A001")
    String code,

    @Schema(description = "카테고리. ANIMAL(동물), PLANT(식물), PLACE(장소) 중 하나.", example = "ANIMAL")
    CatalogCategory category,

    @Schema(description = "이름. 미수집 시 null(프론트에서 '?'로 표시).", example = "다람쥐")
    String name,

    @Schema(description = "이미지 URL. 미수집 시 null(프론트에서 '?' 이미지로 표시).", example = "https://cdn.eodaego.com/animals/squirrel.png")
    String imageUrl,

    @Schema(description = "수집 가능 상태. AVAILABLE(수집 가능), SUSPENDED(임시 중단), RETIRED(운영 종료) 중 하나.", example = "AVAILABLE")
    CatalogItemStatus status,

    @Schema(description = "현재 로그인한 회원의 수집 여부", example = "true")
    boolean collected
) {

  public static CatalogItemSummaryResponse from(CatalogItem catalogItem, String code, boolean collected) {
    return new CatalogItemSummaryResponse(
        catalogItem.getId(),
        code,
        catalogItem.getCategory(),
        collected ? catalogItem.getName() : null,
        collected ? catalogItem.getImageUrl() : null,
        catalogItem.getStatus(),
        collected
    );
  }
}