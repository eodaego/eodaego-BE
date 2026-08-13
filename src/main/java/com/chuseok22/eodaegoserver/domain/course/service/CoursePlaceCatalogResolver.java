package com.chuseok22.eodaegoserver.domain.course.service;

import com.chuseok22.eodaegoserver.domain.catalog.CatalogCategory;
import com.chuseok22.eodaegoserver.domain.catalog.entity.CatalogItem;
import com.chuseok22.eodaegoserver.domain.catalog.repository.CatalogItemRepository;
import com.chuseok22.eodaegoserver.domain.catalog.repository.MemberCatalogCollectionRepository;
import com.chuseok22.eodaegoserver.domain.course.dto.response.CoursePlaceCatalogInfo;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CoursePlaceCatalogResolver {

  private final CatalogItemRepository catalogItemRepository;
  private final MemberCatalogCollectionRepository memberCatalogCollectionRepository;

  public Map<Long, CoursePlaceCatalogInfo> resolve(UUID memberId, Collection<Long> facilityIds) {
    List<Long> distinctFacilityIds = facilityIds.stream()
        .distinct()
        .toList();

    if (distinctFacilityIds.isEmpty()) {
      return Map.of();
    }

    List<CatalogItem> catalogItems = catalogItemRepository
        .findByCategoryAndFacility_AiFacilityIdIn(CatalogCategory.PLACE, distinctFacilityIds);

    Set<UUID> collectedCatalogItemIds = findCollectedCatalogItemIds(memberId, catalogItems);

    return catalogItems.stream().collect(Collectors.toMap(
        CatalogItem::getExternalId,
        catalogItem -> new CoursePlaceCatalogInfo(
            catalogItem.getId(),
            collectedCatalogItemIds.contains(catalogItem.getId())
        )
    ));
  }

  private Set<UUID> findCollectedCatalogItemIds(UUID memberId, List<CatalogItem> catalogItems) {
    List<UUID> catalogItemIds = catalogItems.stream()
        .map(CatalogItem::getId)
        .toList();

    if (catalogItemIds.isEmpty()) {
      return Set.of();
    }

    return memberCatalogCollectionRepository
        .findByMemberIdAndCatalogItemIdIn(memberId, catalogItemIds).stream()
        .map(collection -> collection.getCatalogItem().getId())
        .collect(Collectors.toSet());
  }
}
