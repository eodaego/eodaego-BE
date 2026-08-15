package com.chuseok22.eodaegoserver.domain.admin.service;

import com.chuseok22.eodaegoserver.domain.admin.dto.response.CatalogItemEditView;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.CrawlResultView;
import com.chuseok22.eodaegoserver.domain.catalog.CatalogCategory;
import com.chuseok22.eodaegoserver.domain.catalog.dto.request.CatalogItemUpdateRequest;
import com.chuseok22.eodaegoserver.domain.catalog.dto.response.CatalogItemResponse;
import com.chuseok22.eodaegoserver.domain.catalog.entity.CatalogItem;
import com.chuseok22.eodaegoserver.domain.catalog.service.CatalogItemService;
import com.chuseok22.eodaegoserver.domain.catalog.service.CatalogSyncResult;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminCatalogItemService {

  private final CatalogItemService catalogItemService;

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
  }

  public CrawlResultView triggerSync() {
    CatalogSyncResult result = catalogItemService.syncFromAiServer();
    int totalCount = result.created().size() + result.updated().size();
    return new CrawlResultView(true, totalCount, "동기화 완료");
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
