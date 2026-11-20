package com.chuseok22.eodaegoserver.domain.admin.dto.response;

import com.chuseok22.eodaegoserver.domain.catalog.CatalogCategory;

public record CategoryCountView(
    CatalogCategory category,
    long count
) {

}
