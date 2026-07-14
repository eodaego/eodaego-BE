package com.chuseok22.eodaegoserver.domain.admin.service;

import com.chuseok22.eodaegoserver.domain.admin.dto.response.AnimalView;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.CongestionView;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.FacilityView;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.OperatingHoursView;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.PlantView;
import com.chuseok22.eodaegoserver.global.exception.CustomException;
import com.chuseok22.eodaegoserver.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminAiCatalogService {

  private final RestClient aiServerRestClient;

  public List<FacilityView> listFacilities() {
    return get("/api/v1/facility", new ParameterizedTypeReference<List<FacilityView>>() {});
  }

  public List<OperatingHoursView> listOperatingHours() {
    return get("/api/v1/facility/operating-hours", new ParameterizedTypeReference<List<OperatingHoursView>>() {});
  }

  public List<AnimalView> listAnimals() {
    return get("/api/v1/catalog/animals", new ParameterizedTypeReference<List<AnimalView>>() {});
  }

  public List<PlantView> listPlants() {
    return get("/api/v1/catalog/plants", new ParameterizedTypeReference<List<PlantView>>() {});
  }

  public List<CongestionView> listCongestion() {
    return get("/api/v1/congestion", new ParameterizedTypeReference<List<CongestionView>>() {});
  }

  private <T> List<T> get(String uri, ParameterizedTypeReference<List<T>> responseType) {
    try {
      return aiServerRestClient.get()
          .uri(uri)
          .retrieve()
          .body(responseType);
    } catch (RestClientException e) {
      log.warn("[AdminAiCatalogService] AI 서버 조회 실패: uri={}, message={}", uri, e.getMessage());
      throw new CustomException(ErrorCode.AI_SERVER_UNAVAILABLE);
    }
  }
}
