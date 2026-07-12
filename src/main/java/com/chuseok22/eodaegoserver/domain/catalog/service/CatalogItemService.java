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
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
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
  public List<CatalogItem> syncFromAiServer() {
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

    List<CatalogItem> createdItems = new ArrayList<>();
    createdItems.addAll(syncAnimals(animals, extractExternalIds(existingItems, CatalogCategory.ANIMAL)));
    createdItems.addAll(syncPlants(plants, extractExternalIds(existingItems, CatalogCategory.PLANT)));
    createdItems.addAll(syncFacilities(facilities, extractExternalIds(existingItems, CatalogCategory.PLACE)));

    log.info("도감 항목 외부 동기화 완료. 신규 등록 {}건", createdItems.size());
    return createdItems;
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

  private List<CatalogItem> syncAnimals(List<AiAnimalResponse> externalAnimals, Set<Long> existingExternalIds) {
    int sequenceNumber = nextSequenceNumber(CatalogCategory.ANIMAL);
    List<CatalogItem> createdItems = new ArrayList<>();
    for (AiAnimalResponse external : externalAnimals) {
      if (existingExternalIds.contains(external.id())) {
        continue;
      }
      createdItems.add(catalogItemRepository.save(CatalogItem.builder()
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
    return createdItems;
  }

  private List<CatalogItem> syncPlants(List<AiPlantResponse> externalPlants, Set<Long> existingExternalIds) {
    int sequenceNumber = nextSequenceNumber(CatalogCategory.PLANT);
    List<CatalogItem> createdItems = new ArrayList<>();
    for (AiPlantResponse external : externalPlants) {
      if (existingExternalIds.contains(external.id())) {
        continue;
      }
      createdItems.add(catalogItemRepository.save(CatalogItem.builder()
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
    return createdItems;
  }

  private List<CatalogItem> syncFacilities(List<AiFacilityResponse> externalFacilities, Set<Long> existingExternalIds) {
    int sequenceNumber = nextSequenceNumber(CatalogCategory.PLACE);
    List<CatalogItem> createdItems = new ArrayList<>();
    for (AiFacilityResponse external : externalFacilities) {
      if (existingExternalIds.contains(external.id())) {
        continue;
      }
      createdItems.add(catalogItemRepository.save(CatalogItem.builder()
          .sequenceNumber(sequenceNumber++)
          .category(CatalogCategory.PLACE)
          .name(external.name())
          .feature("")
          .childDescription("")
          .status(CatalogItemStatus.AVAILABLE)
          .latitude(external.latitude())
          .longitude(external.longitude())
          .externalId(external.id())
          .build()));
    }
    return createdItems;
  }

  private int nextSequenceNumber(CatalogCategory category) {
    return catalogItemRepository.findTopByCategoryOrderBySequenceNumberDesc(category)
               .map(CatalogItem::getSequenceNumber)
               .orElse(0) + 1;
  }

  private Set<Long> extractExternalIds(List<CatalogItem> catalogItems, CatalogCategory category) {
    return catalogItems.stream()
        .filter(catalogItem -> catalogItem.getCategory() == category)
        .map(CatalogItem::getExternalId)
        .collect(Collectors.toSet());
  }

}
