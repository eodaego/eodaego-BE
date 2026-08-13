package com.chuseok22.eodaegoserver.domain.facility.service;

import com.chuseok22.eodaegoserver.domain.facility.dto.external.AiFacilityResponse;
import com.chuseok22.eodaegoserver.domain.facility.entity.Facility;
import com.chuseok22.eodaegoserver.domain.facility.repository.FacilityRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacilitySyncService {

  private final FacilityRepository facilityRepository;
  private final FacilityAiClient facilityAiClient;
  private final Clock clock;

  @Transactional
  public List<Facility> sync() {
    List<AiFacilityResponse> externals = facilityAiClient.fetchFacilities();
    LocalDateTime lastSeenAt = LocalDateTime.now(clock);

    Map<Long, Facility> existingByAiFacilityId = facilityRepository.findAll().stream()
        .collect(Collectors.toMap(Facility::getAiFacilityId, Function.identity()));

    List<Facility> facilities = new ArrayList<>();
    List<UUID> seenIds = new ArrayList<>();
    int createdCount = 0;
    int updatedCount = 0;

    for (AiFacilityResponse external : externals) {
      Facility existing = existingByAiFacilityId.get(external.id());

      if (existing == null) {
        facilities.add(facilityRepository.save(toNewFacility(external, lastSeenAt)));
        createdCount++;
        continue;
      }

      seenIds.add(existing.getId());

      if (hasChanged(existing, external)) {
        existing.updateFromExternal(
            external.code(), external.category(), external.name(), external.intro(),
            external.description(), external.latitude(), external.longitude(), external.facilityType());
        updatedCount++;
      }

      facilities.add(existing);
    }

    if (!seenIds.isEmpty()) {
      facilityRepository.markLastSeen(seenIds, lastSeenAt);
    }

    log.info("시설 동기화 완료. 신규 {}건, 갱신 {}건, 전체 {}건", createdCount, updatedCount, facilities.size());
    return facilities;
  }

  private boolean hasChanged(Facility facility, AiFacilityResponse external) {
    return !Objects.equals(facility.getCode(), external.code())
           || !Objects.equals(facility.getSourceCategory(), external.category())
           || !Objects.equals(facility.getName(), external.name())
           || !Objects.equals(facility.getIntro(), external.intro())
           || !Objects.equals(facility.getDescription(), external.description())
           || !Objects.equals(facility.getLatitude(), external.latitude())
           || !Objects.equals(facility.getLongitude(), external.longitude())
           || !Objects.equals(facility.getFacilityType(), external.facilityType());
  }

  private Facility toNewFacility(AiFacilityResponse external, LocalDateTime lastSeenAt) {
    return Facility.builder()
        .aiFacilityId(external.id())
        .code(external.code())
        .sourceCategory(external.category())
        .name(external.name())
        .intro(external.intro())
        .description(external.description())
        .latitude(external.latitude())
        .longitude(external.longitude())
        .facilityType(external.facilityType())
        .lastSeenAt(lastSeenAt)
        .build();
  }
}
