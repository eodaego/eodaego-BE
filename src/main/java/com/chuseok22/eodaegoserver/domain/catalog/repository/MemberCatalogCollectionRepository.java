package com.chuseok22.eodaegoserver.domain.catalog.repository;

import com.chuseok22.eodaegoserver.domain.catalog.CatalogCategory;
import com.chuseok22.eodaegoserver.domain.catalog.entity.MemberCatalogCollection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberCatalogCollectionRepository extends JpaRepository<MemberCatalogCollection, UUID> {

  List<MemberCatalogCollection> findByMemberIdAndCatalogItemIdIn(UUID memberId, List<UUID> catalogItemIds);

  Optional<MemberCatalogCollection> findByMemberIdAndCatalogItemId(UUID memberId, UUID catalogItemId);

  /**
   * 수집 기록을 넣되 이미 있으면 아무것도 하지 않는다. 반환값은 실제 삽입된 행 수(0 또는 1).
   */
  @Modifying
  @Query(value = """
      INSERT INTO member_catalog_collection (id, member_id, catalog_item_id, collected_at, created_at, updated_at)
      VALUES (gen_random_uuid(), :memberId, :catalogItemId, :collectedAt, :collectedAt, :collectedAt)
      ON CONFLICT (member_id, catalog_item_id) DO NOTHING
      """, nativeQuery = true)
  int insertIfAbsent(@Param("memberId") UUID memberId,
                     @Param("catalogItemId") UUID catalogItemId,
                     @Param("collectedAt") LocalDateTime collectedAt);

  @Query("""
      SELECT collection FROM MemberCatalogCollection collection
      JOIN FETCH collection.catalogItem item
      LEFT JOIN FETCH item.source source
      LEFT JOIN FETCH item.facility facility
      WHERE collection.member.id = :memberId
        AND COALESCE(item.nameOverride, source.name, facility.name) LIKE CONCAT('%', :name, '%')
      """)
  List<MemberCatalogCollection> findCollectedByMemberIdAndName(
      @Param("memberId") UUID memberId, @Param("name") String name);

  @Query("""
      SELECT collection FROM MemberCatalogCollection collection
      JOIN FETCH collection.catalogItem item
      LEFT JOIN FETCH item.source source
      LEFT JOIN FETCH item.facility facility
      WHERE collection.member.id = :memberId
        AND item.category = :category
        AND COALESCE(item.nameOverride, source.name, facility.name) LIKE CONCAT('%', :name, '%')
      """)
  List<MemberCatalogCollection> findCollectedByMemberIdAndCategoryAndName(
      @Param("memberId") UUID memberId, @Param("category") CatalogCategory category, @Param("name") String name);

  long countByMemberId(UUID memberId);

  long countByMemberIdAndCatalogItem_Category(UUID memberId, CatalogCategory category);

  @Query(value = """
    SELECT ci.category AS category, COUNT(*) AS COUNT
    FROM member_catalog_collection mcc
    JOIN catalog_item ci ON ci.id = mcc.catalog_item_id
    WHERE mcc.member_id = :memberId
    GROUP BY ci.category
    """, nativeQuery = true)
  List<CategoryCountProjection> countCollectedGroupByCategory(@Param("memberId") UUID memberId);
}
