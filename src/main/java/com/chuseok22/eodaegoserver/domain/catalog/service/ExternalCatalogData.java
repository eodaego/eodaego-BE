package com.chuseok22.eodaegoserver.domain.catalog.service;

import com.chuseok22.eodaegoserver.domain.catalog.CatalogCategory;

public record ExternalCatalogData(

    CatalogCategory category,

    Long externalId,

    String name,

    String imageUrl,

    String description

) {

}
