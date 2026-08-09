package com.chuseok22.eodaegoserver.domain.facility.service;

import com.chuseok22.eodaegoserver.domain.facility.dto.external.AiFacilityResponse;
import com.chuseok22.eodaegoserver.domain.facility.dto.response.FacilityDetailResponse;
import com.chuseok22.eodaegoserver.domain.facility.dto.response.FacilitySummaryResponse;
import com.chuseok22.eodaegoserver.domain.facility.dto.response.OperatingHoursResponse;
import com.chuseok22.eodaegoserver.global.exception.CustomException;
import com.chuseok22.eodaegoserver.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FacilityService {

  private final FacilityAiClient facilityAiClient;

  public List<FacilitySummaryResponse> getFacilities() {
    return facilityAiClient.fetchFacilities().stream()
      .map(FacilitySummaryResponse::from)
      .toList();
  }

  public FacilityDetailResponse getFacility(Long facilityId) {

    AiFacilityResponse facility = facilityAiClient.fetchFacilities().stream()
      .filter(item -> item.id().equals(facilityId))
      .findFirst()
      .orElseThrow(() -> new CustomException(ErrorCode.FACILITY_NOT_FOUND));

    List<OperatingHoursResponse> operatingHours =
      facilityAiClient.fetchOperatingHours().stream()
        .map(OperatingHoursResponse::from)
        .toList();

    return FacilityDetailResponse.from(facility, operatingHours);
  }
}
