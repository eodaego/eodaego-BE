package com.chuseok22.eodaegoserver.domain.admin.dto.response;

public record CatalogCrawlResultView(
    CrawlResultView animals,
    CrawlResultView plants,
    CrawlResultView locations
) {
}
