package com.chuseok22.eodaegoserver.domain.catalog.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record CatalogSummaryResponse(

    @Schema(description = "전체 도감 항목 개수(모든 카테고리 합)", example = "80")
    long totalCount,

    @Schema(description = "현재 회원이 수집한 전체 개수(모든 카테고리 합)", example = "24")
    long collectedCount,

    @Schema(description = "전체 수집률(백분율, 소수점 첫째자리까지 반올림, 0~100)", example = "30.5")
    double collectionRate,

    @Schema(description = "카테고리별 수집 현황")
    List<CatalogCategorySummaryResponse> byCategory

) {

}
