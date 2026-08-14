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

  @EntityGraph(attributePaths = {"source", "facility"})
  Optional<CatalogItem> findByCategoryAndStatusAndSource_ExternalId(
      CatalogCategory category, CatalogItemStatus status, Long externalId);

  @EntityGraph(attributePaths = {"source", "facility"})
  Optional<CatalogItem> findByCategoryAndStatusAndFacility_AiFacilityId(
      CatalogCategory category, CatalogItemStatus status, Long aiFacilityId);
  
  @Query(value = "SELECT id FROM catalog_item WHERE category = :category AND id <> :excludeId ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
  List<UUID> findRandomIdsByCategoryExcluding(@Param("category") String category, @Param("excludeId") UUID excludeId, @Param("limit") int limit);

  // PLACE 항목은 facility_id가 채워져 있어 프록시가 생성된다. facility를 함께 fetch하지 않으면
  // 트랜잭션 밖에서 실행되는 퀴즈 선택지 생성(getName())에서 LazyInitializationException이 발생한다.
  @Override
  @EntityGraph(attributePaths = {"source", "facility"})
  List<CatalogItem> findAllById(Iterable<UUID> ids);

  @Query(value = "SELECT category AS category, COUNT(*) AS count FROM catalog_item GROUP BY category", nativeQuery = true)
  List<CategoryCountProjection> countGroupByCategory();
}
