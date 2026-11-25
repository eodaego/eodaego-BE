package com.chuseok22.eodaegoserver.domain.admin.service;

import com.chuseok22.eodaegoserver.domain.admin.CrawlJobType;
import com.chuseok22.eodaegoserver.domain.admin.dto.request.CatalogItemListQuery;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.AdminActionResult;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.AdminPageView;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.CatalogItemEditView;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.CrawlResultView;
import com.chuseok22.eodaegoserver.domain.catalog.CatalogCategory;
import com.chuseok22.eodaegoserver.domain.catalog.CatalogItemStatus;
import com.chuseok22.eodaegoserver.global.exception.CustomException;
import com.chuseok22.eodaegoserver.global.exception.ErrorCode;
import com.chuseok22.eodaegoserver.domain.catalog.dto.request.CatalogItemUpdateRequest;
import com.chuseok22.eodaegoserver.domain.catalog.dto.response.CatalogItemResponse;
import com.chuseok22.eodaegoserver.domain.catalog.entity.CatalogItem;
import com.chuseok22.eodaegoserver.domain.catalog.repository.CatalogItemRepository;
import com.chuseok22.eodaegoserver.domain.catalog.service.CatalogItemService;
import com.chuseok22.eodaegoserver.domain.catalog.service.CatalogSyncResult;
import com.chuseok22.eodaegoserver.domain.catalog.service.CatalogSyncService;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminCatalogItemService {

  private static final List<String> CSV_HEADER = List.of("카테고리", "발번", "이름", "상태", "특징 설명", "어린이 설명");
  private static final Comparator<CatalogItemResponse> DEFAULT_COMPARATOR =
      Comparator.comparing(CatalogItemResponse::category).thenComparingInt(CatalogItemResponse::sequenceNumber);
  private static final Map<String, Comparator<CatalogItemResponse>> SORT_COMPARATORS = Map.of(
      "category", Comparator.comparing(CatalogItemResponse::category),
      "sequenceNumber", Comparator.comparingInt(CatalogItemResponse::sequenceNumber),
      "name", Comparator.comparing(CatalogItemResponse::name, String.CASE_INSENSITIVE_ORDER),
      "status", Comparator.comparing(CatalogItemResponse::status),
      "written", Comparator.comparing((CatalogItemResponse item) -> !isChildDescriptionMissing(item))
  );

  private final CatalogItemService catalogItemService;
  private final CatalogItemRepository catalogItemRepository;
  private final CatalogSyncService catalogSyncService;
  private final CrawlExecutionLogService crawlExecutionLogService;

  public AdminPageView<CatalogItemResponse> listItems(CatalogItemListQuery query) {
    return AdminPageView.of(findFilteredSortedItems(query), query.page(), AdminListSupport.PAGE_SIZE);
  }

  // 도감 항목은 소규모 테이블이라 조건에 맞는 전체 결과를 메모리에 올려 CSV로 만든다고 가정한다.
  public byte[] exportItemsCsv(CatalogItemListQuery query) {
    List<List<String>> rows = findFilteredSortedItems(query).stream()
        .map(item -> List.of(
            item.category().name(),
            String.valueOf(item.sequenceNumber()),
            item.name(),
            item.status().name(),
            Objects.requireNonNullElse(item.feature(), ""),
            Objects.requireNonNullElse(item.childDescription(), "")))
        .toList();
    return AdminCsvWriter.write(CSV_HEADER, rows);
  }

  @Transactional
  public AdminActionResult bulkChangeStatus(List<UUID> catalogItemIds, CatalogItemStatus status) {
    if (catalogItemIds == null || catalogItemIds.isEmpty()) {
      return new AdminActionResult(false, "상태를 변경할 항목을 선택해 주세요.");
    }
    if (status != CatalogItemStatus.AVAILABLE && status != CatalogItemStatus.SUSPENDED) {
      log.warn("관리자 도감 일괄 상태 변경 거부. 허용되지 않은 상태: {}", status);
      return new AdminActionResult(false, "노출 또는 노출 중지로만 변경할 수 있습니다.");
    }
    int requestedCount = (int) catalogItemIds.stream().distinct().count();
    List<CatalogItem> changeTargets = catalogItemRepository.findAllById(catalogItemIds).stream()
        .filter(catalogItem -> catalogItem.getStatus() != CatalogItemStatus.RETIRED)
        .filter(catalogItem -> catalogItem.getStatus() != status)
        .toList();
    changeTargets.forEach(catalogItem -> catalogItem.updateStatus(status));
    int skippedCount = requestedCount - changeTargets.size();
    log.info("관리자 도감 일괄 상태 변경 완료. status={}, 변경 {}건, 건너뜀 {}건", status, changeTargets.size(), skippedCount);
    String statusLabel = status == CatalogItemStatus.AVAILABLE ? "노출" : "노출 중지";
    String skippedMessage = skippedCount > 0 ? " (운영 종료·이미 같은 상태·존재하지 않는 항목 " + skippedCount + "건 제외)" : "";
    return new AdminActionResult(true, changeTargets.size() + "건을 " + statusLabel + " 상태로 변경했습니다." + skippedMessage);
  }

  private List<CatalogItemResponse> findFilteredSortedItems(CatalogItemListQuery query) {
    CatalogCategory categoryFilter = parseCategory(query.category());
    Comparator<CatalogItemResponse> comparator =
        AdminSortResolver.resolve(SORT_COMPARATORS, query.sort(), query.direction(), DEFAULT_COMPARATOR)
            .thenComparing(DEFAULT_COMPARATOR);
    return catalogItemService.getAllCatalogItems().stream()
        .map(CatalogItemResponse::from)
        .filter(item -> categoryFilter == null || item.category() == categoryFilter)
        .filter(item -> !query.onlyMissingChildDescription() || isChildDescriptionMissing(item))
        .filter(AdminListSupport.keywordFilter(query.keyword(), CatalogItemResponse::name))
        .sorted(comparator)
        .toList();
  }

  // 대시보드 미작성 건수(CatalogItemRepository.countMissingChildDescription)와 같은 정의여야 한다. 한쪽을 바꾸면 함께 수정한다.
  private static boolean isChildDescriptionMissing(CatalogItemResponse item) {
    return item.childDescription() == null || item.childDescription().isBlank();
  }

  public CatalogItemEditView getEditView(UUID catalogItemId) {
    CatalogItem catalogItem = catalogItemService.getCatalogItem(catalogItemId);
    boolean isPlace = catalogItem.getFacility() != null;

    String originalName = catalogItem.getOriginalName();
    String aiFeature = isPlace ? catalogItem.getFacility().getDescription() : catalogItem.getSource().getDescription();
    String originalFeature = Objects.requireNonNullElse(
        Objects.requireNonNullElse(catalogItem.getDefaultFeature(), aiFeature), "");
    String originalChildDescription = Objects.requireNonNullElse(catalogItem.getDefaultChildDescription(), "");
    String originalImageUrl = isPlace ? null : catalogItem.getSource().getImageUrl();
    Double originalLatitude = isPlace ? catalogItem.getFacility().getLatitude() : null;
    Double originalLongitude = isPlace ? catalogItem.getFacility().getLongitude() : null;

    CatalogItemUpdateRequest request = new CatalogItemUpdateRequest(
        catalogItem.getNameOverride(),
        catalogItem.getFeatureOverride(),
        catalogItem.getChildDescriptionOverride(),
        catalogItem.getStatus(),
        catalogItem.getImageUrlOverride(),
        catalogItem.getLatitudeOverride(),
        catalogItem.getLongitudeOverride()
    );

    return new CatalogItemEditView(
        CatalogItemResponse.from(catalogItem),
        originalName, originalFeature, originalChildDescription, originalImageUrl, originalLatitude, originalLongitude,
        request
    );
  }

  public void updateItem(UUID catalogItemId, CatalogItemUpdateRequest request) {
    catalogItemService.updateCatalogItem(catalogItemId, normalize(request));
    log.info("관리자 도감 항목 override 수정 완료. catalogItemId={}", catalogItemId);
  }

  public CrawlResultView triggerSync() {
    try {
      CatalogSyncResult result = catalogSyncService.syncFromAiServer();
      int totalCount = result.created().size() + result.updated().size();
      log.info("관리자 도감 동기화 트리거 완료. 생성 {}건, 갱신 {}건", result.created().size(), result.updated().size());
      crawlExecutionLogService.record(CrawlJobType.CATALOG_SYNC, true, totalCount, "동기화 완료");
      return new CrawlResultView(true, totalCount, "동기화 완료");
    } catch (CustomException e) {
      // 크롤링 트리거와 같은 방식으로, 중복 실행은 실패가 아니라 안내로 돌려준다.
      if (e.getErrorCode() == ErrorCode.CATALOG_SYNC_ALREADY_RUNNING) {
        log.info("[AdminCatalogItemService] 이미 실행 중인 도감 동기화");
        crawlExecutionLogService.record(CrawlJobType.CATALOG_SYNC, false, 0, "이미 실행 중입니다");
        return new CrawlResultView(false, 0, "이미 실행 중입니다");
      }
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
