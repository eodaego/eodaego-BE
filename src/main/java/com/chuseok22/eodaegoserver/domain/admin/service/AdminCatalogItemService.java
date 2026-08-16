package com.chuseok22.eodaegoserver.domain.admin.service;

import com.chuseok22.eodaegoserver.domain.admin.CrawlJobType;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.CatalogItemEditView;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.CrawlResultView;
import com.chuseok22.eodaegoserver.domain.catalog.CatalogCategory;
import com.chuseok22.eodaegoserver.global.exception.CustomException;
import com.chuseok22.eodaegoserver.domain.catalog.dto.request.CatalogItemUpdateRequest;
import com.chuseok22.eodaegoserver.domain.catalog.dto.response.CatalogItemResponse;
import com.chuseok22.eodaegoserver.domain.catalog.entity.CatalogItem;
import com.chuseok22.eodaegoserver.domain.catalog.service.CatalogItemService;
import com.chuseok22.eodaegoserver.domain.catalog.service.CatalogSyncResult;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminCatalogItemService {

  private final CatalogItemService catalogItemService;
  private final CrawlExecutionLogService crawlExecutionLogService;

  public List<CatalogItemResponse> listItems(String category) {
    CatalogCategory categoryFilter = parseCategory(category);
    return catalogItemService.getAllCatalogItems().stream()
        .filter(catalogItem -> categoryFilter == null || catalogItem.getCategory() == categoryFilter)
        .map(CatalogItemResponse::from)
        .toList();
  }

  public CatalogItemEditView getEditView(UUID catalogItemId) {
    CatalogItem catalogItem = catalogItemService.getCatalogItem(catalogItemId);
    boolean isPlace = catalogItem.getFacility() != null;

    String originalName = isPlace ? catalogItem.getFacility().getName() : catalogItem.getSource().getName();
    String originalFeature = Objects.requireNonNullElse(
        isPlace ? catalogItem.getFacility().getDescription() : catalogItem.getSource().getDescription(), "");
    String originalImageUrl = isPlace ? null : catalogItem.getSource().getImageUrl();
    Double originalLatitude = isPlace ? catalogItem.getFacility().getLatitude() : null;
    Double originalLongitude = isPlace ? catalogItem.getFacility().getLongitude() : null;

    CatalogItemUpdateRequest request = new CatalogItemUpdateRequest(
        catalogItem.getNameOverride(),
        catalogItem.getFeatureOverride(),
        catalogItem.getChildDescription(),
        catalogItem.getStatus(),
        catalogItem.getImageUrlOverride(),
        catalogItem.getLatitudeOverride(),
        catalogItem.getLongitudeOverride()
    );

    return new CatalogItemEditView(
        CatalogItemResponse.from(catalogItem),
        originalName, originalFeature, originalImageUrl, originalLatitude, originalLongitude,
        request
    );
  }

  public void updateItem(UUID catalogItemId, CatalogItemUpdateRequest request) {
    catalogItemService.updateCatalogItem(catalogItemId, normalize(request));
    log.info("관리자 도감 항목 override 수정 완료. catalogItemId={}", catalogItemId);
  }

  public CrawlResultView triggerSync() {
    try {
      CatalogSyncResult result = catalogItemService.syncFromAiServer();
      int totalCount = result.created().size() + result.updated().size();
      log.info("관리자 도감 동기화 트리거 완료. 생성 {}건, 갱신 {}건", result.created().size(), result.updated().size());
      crawlExecutionLogService.record(CrawlJobType.CATALOG_SYNC, true, totalCount, "동기화 완료");
      return new CrawlResultView(true, totalCount, "동기화 완료");
    } catch (CustomException e) {
      log.warn("관리자 도감 동기화 트리거 실패: {}", e.getMessage());
      crawlExecutionLogService.record(CrawlJobType.CATALOG_SYNC, false, 0, e.getErrorCode().getMessage());
      throw e;
    }
  }

  private CatalogItemUpdateRequest normalize(CatalogItemUpdateRequest request) {
    return new CatalogItemUpdateRequest(
        blankToNull(request.nameOverride()),
        blankToNull(request.featureOverride()),
        request.childDescription(),
        request.status(),
        blankToNull(request.imageUrlOverride()),
        request.latitudeOverride(),
        request.longitudeOverride()
    );
  }

  private String blankToNull(String value) {
    return (value == null || value.isBlank()) ? null : value;
  }

  private CatalogCategory parseCategory(String category) {
    if (category == null || category.isBlank()) {
      return null;
    }
    try {
      return CatalogCategory.valueOf(category);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }
}
