package com.chuseok22.eodaegoserver.domain.admin.dto.response;

import com.chuseok22.eodaegoserver.domain.catalog.dto.request.CatalogItemUpdateRequest;
import com.chuseok22.eodaegoserver.domain.catalog.dto.response.CatalogItemResponse;

public record CatalogItemEditView(
    CatalogItemResponse item,
    String originalName,
    String originalFeature,
    String originalImageUrl,
    Double originalLatitude,
    Double originalLongitude,
    CatalogItemUpdateRequest request
) {

}
