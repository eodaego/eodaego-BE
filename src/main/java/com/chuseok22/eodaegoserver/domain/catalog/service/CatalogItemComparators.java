package com.chuseok22.eodaegoserver.domain.catalog.service;

import com.chuseok22.eodaegoserver.domain.catalog.entity.CatalogItem;
import java.util.Comparator;

public final class CatalogItemComparators {

  public static final Comparator<CatalogItem> DISPLAY_ORDER =
      Comparator.comparing((CatalogItem catalogItem) -> catalogItem.getCategory().getCodePrefix())
          .thenComparingInt(CatalogItem::getSequenceNumber);

  private CatalogItemComparators() {
  }

}
