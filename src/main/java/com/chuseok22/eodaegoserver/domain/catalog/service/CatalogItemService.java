package com.chuseok22.eodaegoserver.domain.catalog.service;

import com.chuseok22.eodaegoserver.domain.catalog.CatalogCategory;
import com.chuseok22.eodaegoserver.domain.catalog.CatalogItemStatus;
import com.chuseok22.eodaegoserver.domain.catalog.dto.external.AiAnimalResponse;
import com.chuseok22.eodaegoserver.domain.catalog.dto.external.AiPlantResponse;
import com.chuseok22.eodaegoserver.domain.catalog.dto.request.CatalogItemStatusUpdateRequest;
import com.chuseok22.eodaegoserver.domain.catalog.dto.request.CatalogItemUpdateRequest;
import com.chuseok22.eodaegoserver.domain.catalog.entity.CatalogItem;
import com.chuseok22.eodaegoserver.domain.catalog.entity.CatalogSource;
import com.chuseok22.eodaegoserver.domain.catalog.repository.CatalogItemRepository;
import com.chuseok22.eodaegoserver.domain.catalog.repository.CatalogSourceRepository;
import com.chuseok22.eodaegoserver.domain.facility.entity.Facility;
import com.chuseok22.eodaegoserver.domain.facility.service.FacilitySyncService;
import com.chuseok22.eodaegoserver.global.exception.CustomException;
import com.chuseok22.eodaegoserver.global.exception.ErrorCode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CatalogItemService {

  private static final String GATE_CATEGORY = "출입문";
  private static final String INFORMATION_FACILITY_TYPE = "안내";
  private static final List<CatalogCategory> SOURCE_BACKED_CATEGORIES =
      List.of(CatalogCategory.ANIMAL, CatalogCategory.PLANT);

  private final CatalogItemRepository catalogItemRepository;
  private final CatalogSourceRepository catalogSourceRepository;
  private final CatalogAiClient catalogAiClient;
  private final FacilitySyncService facilitySyncService;
  private final Clock clock;

  @Transactional
  public CatalogSyncResult syncFromAiServer() {
    Map<CatalogCategory, List<ExternalCatalogData>> externalsByCategory = fetchExternals().stream()
        .collect(Collectors.groupingBy(ExternalCatalogData::category));

    LocalDateTime lastSeenAt = LocalDateTime.now(clock);
    List<CatalogItem> createdItems = new ArrayList<>();
    List<CatalogItem> updatedItems = new ArrayList<>();
    List<UUID> seenSourceIds = new ArrayList<>();

    for (CatalogCategory category : SOURCE_BACKED_CATEGORIES) {
      syncCategory(category, externalsByCategory.getOrDefault(category, List.of()),
          lastSeenAt, createdItems, updatedItems, seenSourceIds);
    }

    if (!seenSourceIds.isEmpty()) {
      catalogSourceRepository.markLastSeen(seenSourceIds, lastSeenAt);
    }

    syncPlaceItems(createdItems);

    log.info("도감 항목 외부 동기화 완료. 신규 등록 {}건, 원본 갱신 {}건, 확인 {}건",
        createdItems.size(), updatedItems.size(), seenSourceIds.size());
    return new CatalogSyncResult(createdItems, updatedItems);
  }

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

    catalogItem.update(
        request.nameOverride(),
        request.featureOverride(),
        request.childDescription(),
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

  private void syncCategory(
      CatalogCategory category, List<ExternalCatalogData> externals, LocalDateTime lastSeenAt,
      List<CatalogItem> createdItems, List<CatalogItem> updatedItems, List<UUID> seenSourceIds) {

    if (externals.isEmpty()) {
      return;
    }

    Map<Long, CatalogSource> sourcesByExternalId = catalogSourceRepository.findByCategory(category).stream()
        .collect(Collectors.toMap(CatalogSource::getExternalId, Function.identity()));
    Map<UUID, CatalogItem> itemsBySourceId = catalogItemRepository.findByCategory(category).stream()
        .collect(Collectors.toMap(catalogItem -> catalogItem.getSource().getId(), Function.identity()));

    int sequenceNumber = nextSequenceNumber(category);

    for (ExternalCatalogData external : externals) {
      CatalogSource existingSource = sourcesByExternalId.get(external.externalId());

      if (existingSource == null) {
        CatalogSource source = catalogSourceRepository.save(toNewSource(external, lastSeenAt));
        createdItems.add(catalogItemRepository.save(toNewCatalogItem(source, sequenceNumber++)));
        continue;
      }

      seenSourceIds.add(existingSource.getId());

      if (!hasChanged(existingSource, external)) {
        continue;
      }

      existingSource.updateFromExternal(external.name(), external.imageUrl(), external.description());

      CatalogItem catalogItem = itemsBySourceId.get(existingSource.getId());
      if (catalogItem != null) {
        updatedItems.add(catalogItem);
      }
    }
  }

  private void syncPlaceItems(List<CatalogItem> createdItems) {
    Map<UUID, CatalogItem> itemsByFacilityId =
        catalogItemRepository.findByCategory(CatalogCategory.PLACE).stream()
            .collect(Collectors.toMap(catalogItem -> catalogItem.getFacility().getId(), Function.identity()));

    int sequenceNumber = nextSequenceNumber(CatalogCategory.PLACE);
    int excludedCount = 0;

    for (Facility facility : facilitySyncService.sync()) {
      if (isExcludedFacility(facility)) {
        excludedCount++;
        continue;
      }
      if (itemsByFacilityId.containsKey(facility.getId())) {
        continue;
      }
      createdItems.add(catalogItemRepository.save(toNewPlaceCatalogItem(facility, sequenceNumber++)));
    }

    log.info("장소 도감 동기화 완료. 도감 제외 {}건(출입문/안내시설)", excludedCount);
  }

  private List<ExternalCatalogData> fetchExternals() {
    List<ExternalCatalogData> externals = new ArrayList<>();

    for (AiAnimalResponse animal : catalogAiClient.fetchAnimals()) {
      externals.add(new ExternalCatalogData(CatalogCategory.ANIMAL, animal.id(), animal.name(),
          animal.thumbnailUrl(), null));
    }

    for (AiPlantResponse plant : catalogAiClient.fetchPlants()) {
      externals.add(new ExternalCatalogData(CatalogCategory.PLANT, plant.id(), plant.name(),
          plant.thumbnailUrl(), plant.description()));
    }

    return externals;
  }

  private boolean hasChanged(CatalogSource source, ExternalCatalogData external) {
    return !Objects.equals(source.getName(), external.name())
           || !Objects.equals(source.getImageUrl(), external.imageUrl())
           || !Objects.equals(source.getDescription(), external.description());
  }

  private CatalogSource toNewSource(ExternalCatalogData external, LocalDateTime lastSeenAt) {
    return CatalogSource.builder()
        .category(external.category())
        .externalId(external.externalId())
        .name(external.name())
        .imageUrl(external.imageUrl())
        .description(external.description())
        .lastSeenAt(lastSeenAt)
        .build();
  }

  private CatalogItem toNewCatalogItem(CatalogSource source, int sequenceNumber) {
    return CatalogItem.builder()
        .source(source)
        .sequenceNumber(sequenceNumber)
        .category(source.getCategory())
        .childDescription("")
        .status(CatalogItemStatus.AVAILABLE)
        .build();
  }

  private CatalogItem toNewPlaceCatalogItem(Facility facility, int sequenceNumber) {
    return CatalogItem.builder()
        .facility(facility)
        .sequenceNumber(sequenceNumber)
        .category(CatalogCategory.PLACE)
        .childDescription("")
        .status(CatalogItemStatus.AVAILABLE)
        .build();
  }

  private boolean isExcludedFacility(Facility facility) {
    String category = facility.getSourceCategory();
    String facilityType = facility.getFacilityType();

    return (category != null && GATE_CATEGORY.equals(category.trim()))
           || (facilityType != null && INFORMATION_FACILITY_TYPE.equals(facilityType.trim()));
  }

  private int nextSequenceNumber(CatalogCategory category) {
    return catalogItemRepository.findTopByCategoryOrderBySequenceNumberDesc(category)
               .map(CatalogItem::getSequenceNumber)
               .orElse(0) + 1;
  }
}
