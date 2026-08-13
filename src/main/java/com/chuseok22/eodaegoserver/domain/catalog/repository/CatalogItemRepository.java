package com.chuseok22.eodaegoserver.domain.catalog.repository;

import com.chuseok22.eodaegoserver.domain.catalog.CatalogCategory;
import com.chuseok22.eodaegoserver.domain.catalog.CatalogItemStatus;
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
  @EntityGraph(attributePaths = {"source", "facility"})
  List<CatalogItem> findAll();

  @EntityGraph(attributePaths = {"source", "facility"})
  List<CatalogItem> findByCategory(CatalogCategory category);

  @Override
  @EntityGraph(attributePaths = {"source", "facility"})
  Optional<CatalogItem> findById(UUID id);

  @EntityGraph(attributePaths = {"source", "facility"})
  List<CatalogItem> findByCategoryAndFacility_AiFacilityIdIn(CatalogCategory category, List<Long> aiFacilityIds);

  @EntityGraph(attributePaths = "source")
  Optional<CatalogItem> findByCategoryAndStatusAndSource_ExternalId(
      CatalogCategory category, CatalogItemStatus status, Long externalId);

  @Query(value = "SELECT id FROM catalog_item WHERE category = :category AND id <> :excludeId ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
  List<UUID> findRandomDistractorIds(@Param("category") String category, @Param("excludeId") UUID excludeId, @Param("limit") int limit);

  @EntityGraph(attributePaths = "source")
  List<CatalogItem> findAllById(Iterable<UUID> ids);

  @Query(value = "SELECT category AS category, COUNT(*) AS count FROM catalog_item GROUP BY category", nativeQuery = true)
  List<CategoryCountProjection> countGroupByCategory();
}
