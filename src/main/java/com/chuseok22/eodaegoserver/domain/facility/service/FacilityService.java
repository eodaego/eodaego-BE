package com.chuseok22.eodaegoserver.domain.facility.service;

import com.chuseok22.eodaegoserver.domain.facility.dto.external.AiFacilityResponse;
import com.chuseok22.eodaegoserver.domain.facility.dto.response.FacilityDetailResponse;
import com.chuseok22.eodaegoserver.domain.facility.dto.response.FacilitySummaryResponse;
import com.chuseok22.eodaegoserver.domain.facility.dto.response.OperatingHoursResponse;
import com.chuseok22.eodaegoserver.global.exception.CustomException;
import com.chuseok22.eodaegoserver.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacilityService {

  private final FacilityAiClient facilityAiClient;

  public List<FacilitySummaryResponse> getFacilities() {
    List<FacilitySummaryResponse> facilities =
      facilityAiClient.fetchFacilities().stream()
        .map(FacilitySummaryResponse::from)
        .toList();

    log.debug("시설 전체 조회 완료. count={}", facilities.size());

    return facilities;
  }

  public FacilityDetailResponse getFacility(Long facilityId) {

    AiFacilityResponse facility = facilityAiClient.fetchFacilities().stream()
      .filter(item -> item.id().equals(facilityId))
      .findFirst()
      .orElseThrow(() -> {
        log.warn("시설 상세 조회 실패. facilityId={}", facilityId);
        return new CustomException(ErrorCode.FACILITY_NOT_FOUND);
      });

    List<OperatingHoursResponse> operatingHours =
      facilityAiClient.fetchOperatingHours().stream()
        .map(OperatingHoursResponse::from)
        .toList();

    return FacilityDetailResponse.from(facility, operatingHours);
  }
}
