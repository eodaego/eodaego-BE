package com.chuseok22.eodaegoserver.domain.catalog.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record CatalogSyncResponse(

    @Schema(description = "이번 동기화에서 새로 등록된 도감 항목 목록")
    List<CatalogItemResponse> created,

    @Schema(description = "이번 동기화에서 원본 값이 바뀌어 갱신된 도감 항목 목록")
    List<CatalogItemResponse> updated

) {

}
