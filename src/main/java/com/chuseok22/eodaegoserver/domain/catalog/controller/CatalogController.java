package com.chuseok22.eodaegoserver.domain.catalog.controller;

import com.chuseok22.eodaegoserver.domain.catalog.CatalogCategory;
import com.chuseok22.eodaegoserver.domain.catalog.dto.response.CatalogItemDetailResponse;
import com.chuseok22.eodaegoserver.domain.catalog.dto.response.CatalogItemListResponse;
import com.chuseok22.eodaegoserver.domain.catalog.dto.response.CatalogSummaryResponse;
import com.chuseok22.eodaegoserver.domain.catalog.service.CatalogService;
import com.chuseok22.logging.annotation.LogMonitoring;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/catalog")
@RequiredArgsConstructor
public class CatalogController implements CatalogControllerDocs {

  private final CatalogService catalogService;

  @Override
  @LogMonitoring
  @GetMapping(path = "", version = "1")
  public ResponseEntity<CatalogItemListResponse> getCatalogItems(
      @AuthenticationPrincipal UUID memberId,
      @RequestParam(required = false) CatalogCategory category,
      @RequestParam(required = false) String name) {
    return ResponseEntity.ok(catalogService.getCatalogItems(memberId, category, name));
  }

  @Override
  @LogMonitoring
  @GetMapping(path = "/{catalogItemId}", version = "1")
  public ResponseEntity<CatalogItemDetailResponse> getCatalogItemDetail(
      @AuthenticationPrincipal UUID memberId,
      @PathVariable UUID catalogItemId) {
    return ResponseEntity.ok(catalogService.getCatalogItemDetail(memberId, catalogItemId));
  }

  @Override
  @LogMonitoring
  @PostMapping(path = "/{catalogItemId}/collect", version = "1")
  public ResponseEntity<Void> collectCatalogItem(
      @AuthenticationPrincipal UUID memberId,
      @PathVariable UUID catalogItemId) {
    catalogService.collectCatalogItem(memberId, catalogItemId);
    return ResponseEntity.noContent().build();
  }

  @Override
  @LogMonitoring
  @GetMapping(path = "/summary", version = "1")
  public ResponseEntity<CatalogSummaryResponse> getCatalogSummary(@AuthenticationPrincipal UUID memberId) {
    return ResponseEntity.ok(catalogService.getCatalogSummary(memberId));
  }

}
