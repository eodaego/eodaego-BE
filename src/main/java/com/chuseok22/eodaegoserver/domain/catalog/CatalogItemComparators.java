package com.chuseok22.eodaegoserver.domain.catalog;

import com.chuseok22.eodaegoserver.domain.catalog.entity.CatalogItem;
import java.util.Comparator;

public final class CatalogItemComparators {

  private CatalogItemComparators() {
  }

  public static final Comparator<CatalogItem> DISPLAY_ORDER =
      Comparator.comparing((CatalogItem catalogItem) -> catalogItem.getCategory().getCodePrefix())
          .thenComparingInt(CatalogItem::getSequenceNumber);

}
