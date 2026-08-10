package com.chuseok22.eodaegoserver.domain.facility.service;

import com.chuseok22.eodaegoserver.domain.facility.dto.external.AiFacilityResponse;
import com.chuseok22.eodaegoserver.global.exception.CustomException;
import com.chuseok22.eodaegoserver.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
@RequiredArgsConstructor
public class FacilityAiClient {

  private static final String FACILITY_URI = "/api/v1/facility";

  private final RestClient aiServerRestClient;

  public List<AiFacilityResponse> fetchFacilities() {
    try {
      List<AiFacilityResponse> response = aiServerRestClient.get()
          .uri(FACILITY_URI)
          .retrieve()
          .body(new ParameterizedTypeReference<>() {
          });

      if (response == null) {
        log.error("AI 서버 시설 응답 본문이 비어 있습니다. uri={}", FACILITY_URI);
        throw new CustomException(ErrorCode.AI_SERVER_UNAVAILABLE);
      }

      return response;

    } catch (RestClientException exception) {
      log.error("AI 서버 시설 조회 실패. uri={}", FACILITY_URI, exception);
      throw new CustomException(ErrorCode.AI_SERVER_UNAVAILABLE);
    }
  }
}
