package com.chuseok22.eodaegoserver.domain.catalog.dto.request;

import com.chuseok22.eodaegoserver.domain.catalog.CatalogItemStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CatalogItemUpdateRequest(
    @NotBlank
    String name,

    @NotBlank
    String feature,

    @NotBlank
    String childDescription,

    @NotNull
    CatalogItemStatus status,

    String imageUrl,

    Double latitude,

    Double longitude
) {

}
