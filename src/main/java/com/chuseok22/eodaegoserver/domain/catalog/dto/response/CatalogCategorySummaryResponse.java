package com.chuseok22.eodaegoserver.domain.catalog.dto.response;

import com.chuseok22.eodaegoserver.domain.catalog.CatalogCategory;
import io.swagger.v3.oas.annotations.media.Schema;

public record CatalogCategorySummaryResponse(

    @Schema(description = "카테고리. ANIMAL(동물), PLANT(식물), PLACE(장소) 중 하나.", example = "ANIMAL")
    CatalogCategory category,

    @Schema(description = "이 카테고리의 전체 도감 항목 개수", example = "48")
    long totalCount,

    @Schema(description = "이 카테고리에서 현재 회원이 수집한 개수", example = "12")
    long collectedCount,

    @Schema(description = "이 카테고리의 수집률(정수 퍼센트, 0~100)", example = "25")
    int collectionRate

) {

}
