package com.chuseok22.eodaegoserver.domain.catalog.repository;

import com.chuseok22.eodaegoserver.domain.catalog.CatalogCategory;
import com.chuseok22.eodaegoserver.domain.catalog.entity.MemberCatalogCollection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberCatalogCollectionRepository extends JpaRepository<MemberCatalogCollection, UUID> {

  List<MemberCatalogCollection> findByMemberIdAndCatalogItemIdIn(UUID memberId, List<UUID> catalogItemIds);

  Optional<MemberCatalogCollection> findByMemberIdAndCatalogItemId(UUID memberId, UUID catalogItemId);

  @Query("""
      SELECT collection FROM MemberCatalogCollection collection
      JOIN FETCH collection.catalogItem item
      JOIN FETCH item.source source
      WHERE collection.member.id = :memberId
        AND COALESCE(NULLIF(TRIM(item.nameOverride), ''), source.name) LIKE CONCAT('%', :name, '%')
      """)
  List<MemberCatalogCollection> findCollectedByMemberIdAndName(
      @Param("memberId") UUID memberId, @Param("name") String name);

  @Query("""
      SELECT collection FROM MemberCatalogCollection collection
      JOIN FETCH collection.catalogItem item
      JOIN FETCH item.source source
      WHERE collection.member.id = :memberId
        AND item.category = :category
        AND COALESCE(NULLIF(TRIM(item.nameOverride), ''), source.name) LIKE CONCAT('%', :name, '%')
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
