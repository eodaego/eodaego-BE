package com.chuseok22.eodaegoserver.domain.catalog.controller;

import com.chuseok22.eodaegoserver.domain.catalog.dto.request.CatalogItemStatusUpdateRequest;
import com.chuseok22.eodaegoserver.domain.catalog.dto.request.CatalogItemUpdateRequest;
import com.chuseok22.eodaegoserver.domain.catalog.dto.response.CatalogItemResponse;
import com.chuseok22.eodaegoserver.domain.catalog.dto.response.CatalogSyncResponse;
import com.chuseok22.eodaegoserver.domain.catalog.service.CatalogItemService;
import com.chuseok22.eodaegoserver.domain.catalog.service.CatalogSyncResult;
import com.chuseok22.logging.annotation.LogMonitoring;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/catalog/items")
@RequiredArgsConstructor
public class CatalogItemController implements CatalogItemControllerDocs {

  private final CatalogItemService catalogItemService;

  @Override
  @LogMonitoring
  @GetMapping(path = "", version = "1")
  public ResponseEntity<List<CatalogItemResponse>> getCatalogItems() {
    List<CatalogItemResponse> response = catalogItemService.getAllCatalogItems().stream()
        .map(CatalogItemResponse::from)
        .toList();
    return ResponseEntity.ok(response);
  }

  @Override
  @LogMonitoring
  @GetMapping(path = "/{catalogItemId}", version = "1")
  public ResponseEntity<CatalogItemResponse> getCatalogItem(@PathVariable UUID catalogItemId) {
    return ResponseEntity.ok(CatalogItemResponse.from(catalogItemService.getCatalogItem(catalogItemId)));
  }

  @Override
  @LogMonitoring
  @PostMapping(path = "/sync", version = "1")
  public ResponseEntity<CatalogSyncResponse> syncCatalogItems() {

    CatalogSyncResult result = catalogItemService.syncFromAiServer();

    List<CatalogItemResponse> created = result.created().stream()
        .map(CatalogItemResponse::from)
        .toList();
    List<CatalogItemResponse> updated = result.updated().stream()
        .map(CatalogItemResponse::from)
        .toList();
    return ResponseEntity.ok(new CatalogSyncResponse(created, updated));
  }

  @Override
  @LogMonitoring
  @PatchMapping(path = "/{catalogItemId}", version = "1")
  public ResponseEntity<Void> updateCatalogItem(
      @PathVariable UUID catalogItemId,
      @Valid @RequestBody CatalogItemUpdateRequest request) {
    catalogItemService.updateCatalogItem(catalogItemId, request);
    return ResponseEntity.noContent().build();
  }

  @Override
  @LogMonitoring
  @PatchMapping(path = "/{catalogItemId}/status", version = "1")
  public ResponseEntity<Void> updateCatalogItemStatus(
      @PathVariable UUID catalogItemId,
      @Valid @RequestBody CatalogItemStatusUpdateRequest request) {
    catalogItemService.updateCatalogItemStatus(catalogItemId, request);
    return ResponseEntity.noContent().build();
  }

  @Override
  @LogMonitoring
  @DeleteMapping(path = "/{catalogItemId}", version = "1")
  public ResponseEntity<Void> deleteCatalogItem(@PathVariable UUID catalogItemId) {
    catalogItemService.deleteCatalogItem(catalogItemId);
    return ResponseEntity.noContent().build();
  }
}
