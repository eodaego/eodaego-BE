package com.chuseok22.eodaegoserver.domain.catalog.repository;

import com.chuseok22.eodaegoserver.domain.catalog.CatalogCategory;
import com.chuseok22.eodaegoserver.domain.catalog.entity.CatalogItem;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CatalogItemRepository extends JpaRepository<CatalogItem, UUID> {

  Optional<CatalogItem> findTopByCategoryOrderBySequenceNumberDesc(CatalogCategory category);

  @Override
  @EntityGraph(attributePaths = "source")
  List<CatalogItem> findAll();

  @EntityGraph(attributePaths = "source")
  List<CatalogItem> findByCategory(CatalogCategory category);

  @Override
  @EntityGraph(attributePaths = "source")
  Optional<CatalogItem> findById(UUID id);

  @EntityGraph(attributePaths = "source")
  List<CatalogItem> findByCategoryAndSource_ExternalIdIn(CatalogCategory category, List<Long> externalIds);

  Optional<CatalogItem> findByCategoryAndExternalId(CatalogCategory category, Long externalId);

  @Query(value = "SELECT * FROM catalog_item WHERE category = :category AND id <> :excludeId ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
  List<CatalogItem> findRandomDistractors(@Param("category") String category, @Param("excludeId") UUID excludeId, @Param("limit") int limit);

  @Query(value = "SELECT category AS category, COUNT(*) AS count FROM catalog_item GROUP BY category", nativeQuery = true)
  List<CategoryCountProjection> countGroupByCategory();
}
