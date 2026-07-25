package com.chuseok22.eodaegoserver.domain.catalog.repository;

import com.chuseok22.eodaegoserver.domain.catalog.CatalogCategory;
import com.chuseok22.eodaegoserver.domain.catalog.entity.CatalogItem;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CatalogItemRepository extends JpaRepository<CatalogItem, UUID> {

  Optional<CatalogItem> findTopByCategoryOrderBySequenceNumberDesc(CatalogCategory category);

  List<CatalogItem> findByCategory(CatalogCategory category);

  List<CatalogItem> findByExternalIdIn(List<Long> externalIds);

  List<CatalogItem> findByCategoryAndExternalIdIn(CatalogCategory category, List<Long> externalIds);

  @Query(value = "SELECT category AS category, COUNT(*) AS count FROM catalog_item GROUP BY category", nativeQuery = true)
  List<CategoryCountProjection> countGroupByCategory();
}
