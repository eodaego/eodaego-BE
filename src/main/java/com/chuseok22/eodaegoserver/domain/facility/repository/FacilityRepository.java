package com.chuseok22.eodaegoserver.domain.facility.repository;

import com.chuseok22.eodaegoserver.domain.facility.entity.Facility;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FacilityRepository extends JpaRepository<Facility, UUID> {

  Optional<Facility> findByAiFacilityId(Long aiFacilityId);

  List<Facility> findAllByOrderByAiFacilityIdAsc();

  @Modifying
  @Query("UPDATE Facility facility SET facility.lastSeenAt = :lastSeenAt WHERE facility.id IN :ids")
  void markLastSeen(@Param("ids") List<UUID> ids, @Param("lastSeenAt") LocalDateTime lastSeenAt);
}
