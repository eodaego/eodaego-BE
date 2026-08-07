package com.chuseok22.eodaegoserver.domain.catalog;

import lombok.Getter;

@Getter
public enum CatalogCategory {

  ANIMAL("A"),
  PLANT("B"),
  PLACE("C");

  private final String codePrefix;

  CatalogCategory(String codePrefix) {
    this.codePrefix = codePrefix;
  }

}
