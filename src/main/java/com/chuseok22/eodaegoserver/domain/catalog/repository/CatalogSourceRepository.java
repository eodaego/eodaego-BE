package com.chuseok22.eodaegoserver.domain.catalog.repository;

import com.chuseok22.eodaegoserver.domain.catalog.CatalogCategory;
import com.chuseok22.eodaegoserver.domain.catalog.entity.CatalogSource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CatalogSourceRepository extends JpaRepository<CatalogSource, UUID> {

  List<CatalogSource> findByCategory(CatalogCategory category);

  @Modifying
  @Query("UPDATE CatalogSource source SET source.lastSeenAt = :lastSeenAt WHERE source.id IN :ids")
  void markLastSeen(@Param("ids") List<UUID> ids, @Param("lastSeenAt") LocalDateTime lastSeenAt);
}
