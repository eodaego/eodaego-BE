package com.chuseok22.eodaegoserver.domain.catalog.service;

import com.chuseok22.eodaegoserver.domain.catalog.CatalogCategory;

public record CatalogDescriptionEntry(

    CatalogCategory category,

    Long externalId,

    String name,

    String feature,

    String childDescription

) {

}
