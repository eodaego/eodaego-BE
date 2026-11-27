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

  // 관리자 도감 목록의 "미작성" 필터(AdminCatalogItemService.isChildDescriptionMissing)와 같은 정의여야
  // 대시보드 수치와 목록 건수가 일치한다. 한쪽을 바꾸면 반드시 함께 수정한다.
  // 어린이 설명은 관리자 수정값(override)이 없으면 설명 파일 기본값(default)을 따르므로, 둘 다 비어 있을 때만 미작성이다.
  @Query(value = """
      SELECT COUNT(*) FROM catalog_item
      WHERE (child_description_override IS NULL OR child_description_override ~ '^\\s*$')
        AND (default_child_description IS NULL OR default_child_description ~ '^\\s*$')
      """, nativeQuery = true)
  long countMissingChildDescription();
}
