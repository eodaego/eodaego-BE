package com.chuseok22.eodaegoserver.domain.catalog.service;

import com.chuseok22.eodaegoserver.domain.catalog.dto.request.CatalogItemStatusUpdateRequest;
import com.chuseok22.eodaegoserver.domain.catalog.dto.request.CatalogItemUpdateRequest;
import com.chuseok22.eodaegoserver.domain.catalog.entity.CatalogItem;
import com.chuseok22.eodaegoserver.domain.catalog.entity.CatalogSource;
import com.chuseok22.eodaegoserver.domain.catalog.repository.CatalogItemRepository;
import com.chuseok22.eodaegoserver.domain.catalog.repository.CatalogSourceRepository;
import com.chuseok22.eodaegoserver.global.exception.CustomException;
import com.chuseok22.eodaegoserver.global.exception.ErrorCode;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CatalogItemService {

  private final CatalogItemRepository catalogItemRepository;
  private final CatalogSourceRepository catalogSourceRepository;

  public List<CatalogItem> getAllCatalogItems() {
    return catalogItemRepository.findAll().stream()
        .sorted(CatalogItemComparators.DISPLAY_ORDER)
        .toList();
  }

  public CatalogItem getCatalogItem(UUID catalogItemId) {
    return catalogItemRepository.findById(catalogItemId)
        .orElseThrow(() -> {
          log.warn("도감 항목 조회 실패. catalogItemId={}", catalogItemId);
          throw new CustomException(ErrorCode.CATALOG_ITEM_NOT_FOUND);
        });
  }

  @Transactional
  public void updateCatalogItem(UUID catalogItemId, CatalogItemUpdateRequest request) {
    CatalogItem catalogItem = getCatalogItem(catalogItemId);

    // 빈 문자열은 "아직 작성하지 않음"으로 보내던 기존 계약을 유지하기 위해 null(기본값을 따름)로 취급한다.
    catalogItem.update(
        request.nameOverride(),
        request.featureOverride(),
        blankToNull(request.childDescription()),
        request.status(),
        request.imageUrlOverride(),
        request.latitudeOverride(),
        request.longitudeOverride()
    );

    log.info("도감 항목 수정 완료. catalogItemId={}", catalogItemId);
  }

  @Transactional
  public void updateCatalogItemStatus(UUID catalogItemId, CatalogItemStatusUpdateRequest request) {
    CatalogItem catalogItem = getCatalogItem(catalogItemId);
    catalogItem.updateStatus(request.status());

    log.info("도감 항목 상태 변경 완료. catalogItemId={}, status={}", catalogItemId, request.status());
  }

  @Transactional
  public void deleteCatalogItem(UUID catalogItemId) {
    CatalogItem catalogItem = getCatalogItem(catalogItemId);
    CatalogSource source = catalogItem.getSource();

    catalogItemRepository.delete(catalogItem);
    catalogItemRepository.flush();

    if (source != null) {
      catalogSourceRepository.delete(source);
    }

    log.info("도감 항목 삭제 완료. catalogItemId={}", catalogItemId);
  }

  private String blankToNull(String value) {
    return (value == null || value.isBlank()) ? null : value;
  }
}
