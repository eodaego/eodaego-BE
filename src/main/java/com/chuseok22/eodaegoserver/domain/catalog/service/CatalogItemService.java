package com.chuseok22.eodaegoserver.domain.catalog.service;

import com.chuseok22.eodaegoserver.domain.catalog.CatalogCategory;
import com.chuseok22.eodaegoserver.domain.catalog.CatalogItemStatus;
import com.chuseok22.eodaegoserver.domain.catalog.dto.external.AiAnimalResponse;
import com.chuseok22.eodaegoserver.domain.catalog.dto.external.AiFacilityResponse;
import com.chuseok22.eodaegoserver.domain.catalog.dto.external.AiPlantResponse;
import com.chuseok22.eodaegoserver.domain.catalog.dto.request.CatalogItemStatusUpdateRequest;
import com.chuseok22.eodaegoserver.domain.catalog.dto.request.CatalogItemUpdateRequest;
import com.chuseok22.eodaegoserver.domain.catalog.entity.CatalogItem;
import com.chuseok22.eodaegoserver.domain.catalog.repository.CatalogItemRepository;
import com.chuseok22.eodaegoserver.global.exception.CustomException;
import com.chuseok22.eodaegoserver.global.exception.ErrorCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
  private final CatalogAiClient catalogAiClient;

  @Transactional
  public CatalogSyncResult syncFromAiServer() {
    List<AiAnimalResponse> animals = catalogAiClient.fetchAnimals();
    List<AiPlantResponse> plants = catalogAiClient.fetchPlants();
    List<AiFacilityResponse> facilities = catalogAiClient.fetchFacilities();

    List<Long> fetchedExternalIds = Stream.of(
            animals.stream().map(AiAnimalResponse::id),
            plants.stream().map(AiPlantResponse::id),
            facilities.stream().map(AiFacilityResponse::id))
        .flatMap(stream -> stream)
        .toList();

    List<CatalogItem> existingItems = catalogItemRepository.findByExternalIdIn(fetchedExternalIds);

    CatalogSyncResult animalResult = syncAnimals(animals, extractExternalId(existingItems, CatalogCategory.ANIMAL));
    CatalogSyncResult plantResult = syncPlants(plants, extractExternalId(existingItems, CatalogCategory.PLANT));
    CatalogSyncResult facilityResult = syncFacilities(facilities, extractExternalId(existingItems, CatalogCategory.PLACE));

    List<CatalogItem> createdItems = new ArrayList<>();
    createdItems.addAll(animalResult.created());
    createdItems.addAll(plantResult.created());
    createdItems.addAll(facilityResult.created());

    List<CatalogItem> updatedItems = new ArrayList<>();
    updatedItems.addAll(animalResult.updated());
    updatedItems.addAll(plantResult.updated());
    updatedItems.addAll(facilityResult.updated());

    log.info("도감 항목 외부 동기화 완료. 신규 등록 {}건, 갱신 {}건", createdItems.size(), updatedItems.size());
    return new CatalogSyncResult(createdItems, updatedItems);
  }

  public List<CatalogItem> getAllCatalogItems() {
    return catalogItemRepository.findAll();
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
        request.name(),
        request.feature(),
        request.childDescription(),
        request.status(),
        request.imageUrl(),
        request.latitude(),
        request.longitude()
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
    catalogItemRepository.delete(catalogItem);

    log.info("도감 항목 삭제 완료. catalogItemId={}", catalogItemId);
  }

  private CatalogSyncResult syncAnimals(List<AiAnimalResponse> externalAnimals, Map<Long, CatalogItem> existingByExternalId) {
    int sequenceNumber = nextSequenceNumber(CatalogCategory.ANIMAL);
    List<CatalogItem> created = new ArrayList<>();
    List<CatalogItem> updated = new ArrayList<>();

    for (AiAnimalResponse external : externalAnimals) {

      CatalogItem existing = existingByExternalId.get(external.id());

      if (existing != null) {
        boolean changed = !Objects.equals(existing.getName(), external.name())
                          || !Objects.equals(existing.getImageUrl(), external.thumbnailUrl());
        if (changed) {
          existing.updateSyncedFields(external.name(), external.thumbnailUrl(), existing.getLatitude(), existing.getLongitude());
          updated.add(existing);
        }
        continue;
      }

      created.add(catalogItemRepository.save(CatalogItem.builder()
          .sequenceNumber(sequenceNumber++)
          .category(CatalogCategory.ANIMAL)
          .name(external.name())
          .feature("")
          .childDescription("")
          .status(CatalogItemStatus.AVAILABLE)
          .imageUrl(external.thumbnailUrl())
          .externalId(external.id())
          .build()));
    }
    return new CatalogSyncResult(created, updated);
  }

  private CatalogSyncResult syncPlants(List<AiPlantResponse> externalPlants, Map<Long, CatalogItem> existingByExternalId) {
    int sequenceNumber = nextSequenceNumber(CatalogCategory.PLANT);
    List<CatalogItem> created = new ArrayList<>();
    List<CatalogItem> updated = new ArrayList<>();

    for (AiPlantResponse external : externalPlants) {

      CatalogItem existing = existingByExternalId.get(external.id());

      if (existing != null) {

        boolean changed = !Objects.equals(existing.getName(), external.name())
                          || !Objects.equals(existing.getImageUrl(), external.thumbnailUrl());
        if (changed) {
          existing.updateSyncedFields(external.name(), external.thumbnailUrl(), existing.getLatitude(), existing.getLongitude());
          updated.add(existing);
        }
        continue;
      }

      created.add(catalogItemRepository.save(CatalogItem.builder()
          .sequenceNumber(sequenceNumber++)
          .category(CatalogCategory.PLANT)
          .name(external.name())
          .feature(Objects.requireNonNullElse(external.description(), ""))
          .childDescription("")
          .status(CatalogItemStatus.AVAILABLE)
          .imageUrl(external.thumbnailUrl())
          .externalId(external.id())
          .build()));
    }
    return new CatalogSyncResult(created, updated);
  }

  private CatalogSyncResult syncFacilities(List<AiFacilityResponse> externalFacilities, Map<Long, CatalogItem> existingByExternalId) {
    int sequenceNumber = nextSequenceNumber(CatalogCategory.PLACE);
    List<CatalogItem> created = new ArrayList<>();
    List<CatalogItem> updated = new ArrayList<>();

    for (AiFacilityResponse external : externalFacilities) {

      CatalogItem existing = existingByExternalId.get(external.id());
      if (existing != null) {
        boolean changed = !Objects.equals(existing.getName(), external.name())
                          || !Objects.equals(existing.getLatitude(), external.latitude())
                          || !Objects.equals(existing.getLongitude(), external.longitude());
        if (changed) {
          existing.updateSyncedFields(external.name(), existing.getImageUrl(), external.latitude(), external.longitude());
          updated.add(existing);
        }
        continue;
      }

      created.add(catalogItemRepository.save(CatalogItem.builder()
          .sequenceNumber(sequenceNumber++)
          .category(CatalogCategory.PLACE)
          .name(external.name())
          .feature(Objects.requireNonNullElse(external.description(), ""))
          .childDescription("")
          .status(CatalogItemStatus.AVAILABLE)
          .latitude(external.latitude())
          .longitude(external.longitude())
          .externalId(external.id())
          .build()));
    }
    return new CatalogSyncResult(created, updated);
  }

  private int nextSequenceNumber(CatalogCategory category) {
    return catalogItemRepository.findTopByCategoryOrderBySequenceNumberDesc(category)
               .map(CatalogItem::getSequenceNumber)
               .orElse(0) + 1;
  }

  private Map<Long, CatalogItem> extractExternalId(List<CatalogItem> catalogItems, CatalogCategory category) {
    return catalogItems.stream()
        .filter(catalogItem -> catalogItem.getCategory() == category)
        .collect(Collectors.toMap(CatalogItem::getExternalId, Function.identity()));
  }
}
