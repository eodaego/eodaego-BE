package com.chuseok22.eodaegoserver.domain.admin.dto.request;

public record CatalogItemListQuery(
    String category,
    boolean onlyMissingChildDescription,
    String keyword,
    String sort,
    String direction,
    int page
) {
}
