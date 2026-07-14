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

  List<MemberCatalogCollection> findByMemberIdAndCatalogItem_NameContaining(UUID memberId, String name);

  List<MemberCatalogCollection> findByMemberIdAndCatalogItem_CategoryAndCatalogItem_NameContaining(
      UUID memberId, CatalogCategory category, String name);

  long countByMemberId(UUID memberId);

  long countByMemberIdAndCatalogItem_Category(UUID memberId, CatalogCategory category);

  @Query(value = """
      SELECT ci.category AS category, COUNT(*) AS count
      FROM member_catalog_collection mcc
      JOIN catalog_item ci ON ci.id = mcc.catalog_item_id
      WHERE mcc.member_id = :memberId
      GROUP BY ci.category
      """, nativeQuery = true)
  List<CategoryCountProjection> countCollectedGroupByCategory(@Param("memberId") UUID memberId);


}
