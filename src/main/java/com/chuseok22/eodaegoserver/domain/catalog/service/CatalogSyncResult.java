package com.chuseok22.eodaegoserver.domain.catalog.service;

import com.chuseok22.eodaegoserver.domain.catalog.entity.CatalogItem;
import java.util.List;

public record CatalogSyncResult(

    List<CatalogItem> created,

    List<CatalogItem> updated) {

}
