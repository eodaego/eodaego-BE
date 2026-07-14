package com.chuseok22.eodaegoserver.domain.catalog.dto.request;

import com.chuseok22.eodaegoserver.domain.catalog.CatalogItemStatus;
import jakarta.validation.constraints.NotNull;

public record CatalogItemStatusUpdateRequest(

    @NotNull
    CatalogItemStatus status

) {

}
