package com.chuseok22.eodaegoserver.domain.catalog.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record CatalogItemListResponse(

    @Schema(description = "현재 카테고리 필터 기준 전체 개수(필터 없으면 전체 도감 항목 수)", example = "48")
    long totalCount,

    @Schema(description = "현재 카테고리 필터 기준 회원이 수집한 개수(필터 없으면 전체 기준)", example = "12")
    long collectedCount,

    @Schema(description = "도감 항목 목록")
    List<CatalogItemSummaryResponse> items

) {

}
