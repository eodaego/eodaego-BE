package com.chuseok22.eodaegoserver.domain.catalog.service;

import com.chuseok22.eodaegoserver.domain.catalog.CatalogCategory;
import com.chuseok22.eodaegoserver.domain.catalog.CatalogItemStatus;
import com.chuseok22.eodaegoserver.domain.catalog.entity.CatalogItem;
import com.chuseok22.eodaegoserver.domain.catalog.entity.CatalogSource;
import com.chuseok22.eodaegoserver.domain.catalog.repository.CatalogItemRepository;
import com.chuseok22.eodaegoserver.domain.catalog.repository.CatalogSourceRepository;
import com.chuseok22.eodaegoserver.domain.facility.dto.external.AiFacilityResponse;
import com.chuseok22.eodaegoserver.domain.facility.entity.Facility;
import com.chuseok22.eodaegoserver.domain.facility.service.FacilitySyncService;
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

/**
 * 도감 동기화의 DB 쓰기 단계만 담당한다.
 *
 * <p>AI 서버 호출은 {@link CatalogSyncService}가 트랜잭션 밖에서 모두 끝낸 뒤 그 결과만 넘겨준다.
 * 외부 호출을 이 트랜잭션 안에 두면 응답을 기다리는 동안 DB 커넥션이 묶여, AI 서버가 느려지는 것만으로
 * 커넥션 풀이 말라 도감과 무관한 요청까지 막힌다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogSyncWriter {

  private static final String GATE_CATEGORY = "출입문";
  private static final String INFORMATION_FACILITY_TYPE = "안내";
  private static final List<CatalogCategory> SOURCE_BACKED_CATEGORIES =
      List.of(CatalogCategory.ANIMAL, CatalogCategory.PLANT);

  private final CatalogItemRepository catalogItemRepository;
  private final CatalogSourceRepository catalogSourceRepository;
  private final FacilitySyncService facilitySyncService;
  private final CatalogDescriptionService catalogDescriptionService;
  private final Clock clock;

  @Transactional
  public CatalogSyncResult write(
      List<ExternalCatalogData> externals, List<AiFacilityResponse> externalFacilities) {

    Map<CatalogCategory, List<ExternalCatalogData>> externalsByCategory = externals.stream()
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

    syncPlaceItems(facilitySyncService.apply(externalFacilities), createdItems);

    // 이번 동기화로 새로 생긴 항목에도 설명 파일의 기본값이 바로 보이도록 같은 트랜잭션에서 반영한다.
    catalogDescriptionService.applyDefaultDescriptions();

    log.info("도감 항목 외부 동기화 완료. 신규 등록 {}건, 원본 갱신 {}건, 확인 {}건",
        createdItems.size(), updatedItems.size(), seenSourceIds.size());
    return new CatalogSyncResult(createdItems, updatedItems);
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

  private void syncPlaceItems(List<Facility> facilities, List<CatalogItem> createdItems) {
    Map<UUID, CatalogItem> itemsByFacilityId =
        catalogItemRepository.findByCategory(CatalogCategory.PLACE).stream()
            .collect(Collectors.toMap(catalogItem -> catalogItem.getFacility().getId(), Function.identity()));

    int sequenceNumber = nextSequenceNumber(CatalogCategory.PLACE);
    int excludedCount = 0;

    for (Facility facility : facilities) {
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
        .status(CatalogItemStatus.AVAILABLE)
        .build();
  }

  private CatalogItem toNewPlaceCatalogItem(Facility facility, int sequenceNumber) {
    return CatalogItem.builder()
        .facility(facility)
        .sequenceNumber(sequenceNumber)
        .category(CatalogCategory.PLACE)
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
