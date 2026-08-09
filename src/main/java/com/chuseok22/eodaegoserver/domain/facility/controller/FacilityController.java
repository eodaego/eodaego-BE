package com.chuseok22.eodaegoserver.domain.facility.controller;

import com.chuseok22.eodaegoserver.domain.facility.dto.response.FacilityDetailResponse;
import com.chuseok22.eodaegoserver.domain.facility.dto.response.FacilitySummaryResponse;
import com.chuseok22.eodaegoserver.domain.facility.service.FacilityService;
import com.chuseok22.logging.annotation.LogMonitoring;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/facilities")
@RequiredArgsConstructor
public class FacilityController implements FacilityControllerDocs {

  private final FacilityService facilityService;

  @Override
  @LogMonitoring
  @GetMapping(path = "", version = "1")
  public ResponseEntity<List<FacilitySummaryResponse>> getFacilities() {
    return ResponseEntity.ok(facilityService.getFacilities());
  }

  @Override
  @LogMonitoring
  @GetMapping(path = "/{facilityId}", version = "1")
  public ResponseEntity<FacilityDetailResponse> getFacility(
    @PathVariable Long facilityId
  ) {
    return ResponseEntity.ok(facilityService.getFacility(facilityId));
  }
}
