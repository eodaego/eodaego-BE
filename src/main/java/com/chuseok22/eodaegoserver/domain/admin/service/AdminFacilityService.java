package com.chuseok22.eodaegoserver.domain.admin.service;

import com.chuseok22.eodaegoserver.domain.admin.dto.request.FacilityCreateRequest;
import com.chuseok22.eodaegoserver.domain.admin.dto.request.FacilityUpdateRequest;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.CrawlResultView;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.FacilityView;
import com.chuseok22.eodaegoserver.global.exception.CustomException;
import com.chuseok22.eodaegoserver.global.exception.ErrorCode;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminFacilityService {

  private static final String BASE_URI = "/api/v1/facility";

  private final RestClient aiServerRestClient;

  public List<FacilityView> listFacilities() {
    try {
      return aiServerRestClient.get()
          .uri(BASE_URI)
          .retrieve()
          .body(new ParameterizedTypeReference<List<FacilityView>>() {});
    } catch (RestClientException e) {
      log.warn("[AdminFacilityService] 시설 목록 조회 실패: {}", e.getMessage());
      throw new CustomException(ErrorCode.AI_SERVER_UNAVAILABLE);
    }
  }

  public List<String> listFacilityCategories() {
    return listFacilities().stream()
        .map(FacilityView::category)
        .filter(Objects::nonNull)
        .distinct()
        .sorted()
        .toList();
  }

  public FacilityView findById(Integer facilityId) {
    return listFacilities().stream()
        .filter(facility -> facility.id().equals(facilityId))
        .findFirst()
        .orElseThrow(() -> new CustomException(ErrorCode.FACILITY_NOT_FOUND));
  }

  public FacilityView create(FacilityCreateRequest request) {
    try {
      return aiServerRestClient.post()
          .uri(BASE_URI)
          .contentType(MediaType.APPLICATION_JSON)
          .body(request)
          .retrieve()
          .body(FacilityView.class);
    } catch (RestClientException e) {
      log.warn("[AdminFacilityService] 시설 생성 실패: {}", e.getMessage());
      throw toCustomException(e);
    }
  }

  public FacilityView update(Integer facilityId, FacilityUpdateRequest request) {
    try {
      return aiServerRestClient.patch()
          .uri(BASE_URI + "/{facilityId}", facilityId)
          .contentType(MediaType.APPLICATION_JSON)
          .body(request)
          .retrieve()
          .body(FacilityView.class);
    } catch (RestClientException e) {
      log.warn("[AdminFacilityService] 시설 수정 실패: facilityId={}, message={}", facilityId, e.getMessage());
      throw toCustomException(e);
    }
  }

  public void delete(Integer facilityId) {
    try {
      aiServerRestClient.delete()
          .uri(BASE_URI + "/{facilityId}", facilityId)
          .retrieve()
          .toBodilessEntity();
    } catch (RestClientException e) {
      log.warn("[AdminFacilityService] 시설 삭제 실패: facilityId={}, message={}", facilityId, e.getMessage());
      throw toCustomException(e);
    }
  }

  public CrawlResultView triggerFacilityImport() {
    try {
      return aiServerRestClient.post()
          .uri(BASE_URI + "/import")
          .retrieve()
          .body(CrawlResultView.class);
    } catch (RestClientResponseException e) {
      if (e.getStatusCode().value() == HttpStatus.CONFLICT.value()) {
        log.info("[AdminFacilityService] 이미 실행 중인 시설 임포트 작업");
        return new CrawlResultView(false, 0, "이미 실행 중입니다");
      }
      log.warn("[AdminFacilityService] 시설 임포트 트리거 실패: {}", e.getMessage());
      throw new CustomException(ErrorCode.AI_SERVER_UNAVAILABLE);
    } catch (RestClientException e) {
      log.warn("[AdminFacilityService] 시설 임포트 트리거 실패: {}", e.getMessage());
      throw new CustomException(ErrorCode.AI_SERVER_UNAVAILABLE);
    }
  }

  private CustomException toCustomException(RestClientException e) {
    if (e instanceof RestClientResponseException responseException) {
      if (responseException.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
        return new CustomException(ErrorCode.FACILITY_NOT_FOUND);
      }
      if (responseException.getStatusCode().value() == HttpStatus.CONFLICT.value()) {
        return new CustomException(ErrorCode.FACILITY_CODE_ALREADY_EXISTS);
      }
      if (responseException.getStatusCode().is4xxClientError()) {
        return new CustomException(ErrorCode.INVALID_REQUEST);
      }
    }
    return new CustomException(ErrorCode.AI_SERVER_UNAVAILABLE);
  }
}
