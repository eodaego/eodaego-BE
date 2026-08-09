package com.chuseok22.eodaegoserver.domain.facility.service;

import com.chuseok22.eodaegoserver.domain.facility.dto.external.AiFacilityResponse;
import com.chuseok22.eodaegoserver.domain.facility.dto.external.AiOperatingHoursResponse;
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

  private final RestClient aiServerRestClient;

  public List<AiFacilityResponse> fetchFacilities() {
    return fetch("/api/v1/facility", new ParameterizedTypeReference<>() {});
  }

  public List<AiOperatingHoursResponse> fetchOperatingHours() {
    return fetch("/api/v1/facility/operating-hours", new ParameterizedTypeReference<>() {});
  }

  private <T> List<T> fetch(
    String uri,
    ParameterizedTypeReference<List<T>> responseType
  ) {
    try {
      return aiServerRestClient.get()
        .uri(uri)
        .retrieve()
        .body(responseType);

    } catch (RestClientException exception) {
      log.error("AI 서버 호출 실패. uri={}", uri, exception);
      throw new CustomException(ErrorCode.AI_SERVER_UNAVAILABLE);
    }
  }
}
