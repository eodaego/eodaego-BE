package com.chuseok22.eodaegoserver.domain.catalog.service;

import com.chuseok22.eodaegoserver.domain.catalog.entity.CatalogItem;
import com.chuseok22.eodaegoserver.domain.catalog.repository.CatalogItemRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogDescriptionService {

  private final CatalogItemRepository catalogItemRepository;
  private final CatalogDescriptionLoader catalogDescriptionLoader;

  /**
   * 설명 파일의 내용을 도감 항목의 기본값(defaultFeature/defaultChildDescription)으로 반영한다.
   * 관리자 수정값(xxxOverride)은 건드리지 않는다. 파일에서 빠진 항목은 기본값이 비워진다.
   */
  @Transactional
  public void applyDefaultDescriptions() {
    List<CatalogItem> catalogItems = catalogItemRepository.findAll();
    Set<CatalogDescriptionEntry> matchedEntries = new HashSet<>();
    int changedCount = 0;

    for (CatalogItem catalogItem : catalogItems) {
      Optional<CatalogDescriptionEntry> entry =
          catalogDescriptionLoader.find(catalogItem.getCategory(), catalogItem.getExternalId());
      entry.ifPresent(matchedEntries::add);
      entry.ifPresent(found -> warnIfNameMismatch(catalogItem, found));

      String defaultFeature = entry.map(CatalogDescriptionEntry::feature).orElse(null);
      String defaultChildDescription = entry.map(CatalogDescriptionEntry::childDescription).orElse(null);

      if (Objects.equals(catalogItem.getDefaultFeature(), defaultFeature)
          && Objects.equals(catalogItem.getDefaultChildDescription(), defaultChildDescription)) {
        continue;
      }

      catalogItem.updateDefaultDescriptions(defaultFeature, defaultChildDescription);
      changedCount++;
    }

    warnUnmatchedEntries(matchedEntries);
    log.info("도감 설명 기본값 반영 완료. 파일 항목 {}건, 매칭 {}건, 변경 {}건",
        catalogDescriptionLoader.getEntries().size(), matchedEntries.size(), changedCount);
  }

  // 키(category + externalId)는 맞는데 이름이 다르면 AI 서버의 id가 바뀌어 엉뚱한 항목에 설명이 붙었을 수 있다.
  private void warnIfNameMismatch(CatalogItem catalogItem, CatalogDescriptionEntry entry) {
    if (entry.name() != null && !entry.name().equals(catalogItem.getOriginalName())) {
      log.warn("도감 설명 파일 이름 불일치. category={}, externalId={}, 파일 이름={}, 원본 이름={}",
          entry.category(), entry.externalId(), entry.name(), catalogItem.getOriginalName());
    }
  }

  private void warnUnmatchedEntries(Set<CatalogDescriptionEntry> matchedEntries) {
    List<String> unmatchedKeys = catalogDescriptionLoader.getEntries().stream()
        .filter(entry -> !matchedEntries.contains(entry))
        .map(entry -> entry.category() + ":" + entry.externalId())
        .toList();

    if (!unmatchedKeys.isEmpty()) {
      log.warn("도감 설명 파일에 있으나 도감 항목이 없어 반영하지 못한 항목 {}건. keys={}", unmatchedKeys.size(), unmatchedKeys);
    }
  }
}
