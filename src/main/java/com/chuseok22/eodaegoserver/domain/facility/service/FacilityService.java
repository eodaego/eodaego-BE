package com.chuseok22.eodaegoserver.domain.facility.service;

import com.chuseok22.eodaegoserver.domain.facility.dto.response.FacilityDetailResponse;
import com.chuseok22.eodaegoserver.domain.facility.dto.response.FacilitySummaryResponse;
import com.chuseok22.eodaegoserver.domain.facility.entity.Facility;
import com.chuseok22.eodaegoserver.domain.facility.repository.FacilityRepository;
import com.chuseok22.eodaegoserver.global.exception.CustomException;
import com.chuseok22.eodaegoserver.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FacilityService {

  private final FacilityRepository facilityRepository;

  public List<FacilitySummaryResponse> getFacilities() {
    List<FacilitySummaryResponse> facilities = facilityRepository.findAllByOrderByAiFacilityIdAsc().stream()
        .map(FacilitySummaryResponse::from)
        .toList();

    log.debug("시설 전체 조회 완료. count={}", facilities.size());

    return facilities;
  }

  public FacilityDetailResponse getFacility(Long facilityId) {
    Facility facility = facilityRepository.findByAiFacilityId(facilityId)
        .orElseThrow(() -> {
          log.warn("시설 상세 조회 실패. facilityId={}", facilityId);
          return new CustomException(ErrorCode.FACILITY_NOT_FOUND);
        });

    return FacilityDetailResponse.from(facility);
  }
}
