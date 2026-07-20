package com.chuseok22.eodaegoserver.domain.admin.service;

import com.chuseok22.eodaegoserver.domain.admin.dto.request.PreferenceCategoryMappingCreateRequest;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.PreferenceCategoryMappingView;
import com.chuseok22.eodaegoserver.global.exception.CustomException;
import com.chuseok22.eodaegoserver.global.exception.ErrorCode;
import java.util.List;
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
public class AdminPreferenceMappingService {

  private static final String BASE_URI = "/api/v1/recommendation/preference-mappings";

  private final RestClient aiServerRestClient;

  public List<PreferenceCategoryMappingView> findAll(String preferenceTag) {
    try {
      return aiServerRestClient.get()
          .uri(uriBuilder -> (preferenceTag == null || preferenceTag.isBlank())
              ? uriBuilder.path(BASE_URI).build()
              : uriBuilder.path(BASE_URI).queryParam("preference_tag", preferenceTag).build())
          .retrieve()
          .body(new ParameterizedTypeReference<List<PreferenceCategoryMappingView>>() {});
    } catch (RestClientException e) {
      log.warn("[AdminPreferenceMappingService] 취향 매핑 목록 조회 실패: {}", e.getMessage());
      throw new CustomException(ErrorCode.AI_SERVER_UNAVAILABLE);
    }
  }

  public PreferenceCategoryMappingView create(PreferenceCategoryMappingCreateRequest request) {
    try {
      return aiServerRestClient.post()
          .uri(BASE_URI)
          .contentType(MediaType.APPLICATION_JSON)
          .body(request)
          .retrieve()
          .body(PreferenceCategoryMappingView.class);
    } catch (RestClientException e) {
      log.warn("[AdminPreferenceMappingService] 취향 매핑 생성 실패: {}", e.getMessage());
      throw toCustomException(e);
    }
  }

  public void delete(Integer mappingId) {
    try {
      aiServerRestClient.delete()
          .uri(BASE_URI + "/{mappingId}", mappingId)
          .retrieve()
          .toBodilessEntity();
    } catch (RestClientException e) {
      log.warn("[AdminPreferenceMappingService] 취향 매핑 삭제 실패: mappingId={}, message={}", mappingId, e.getMessage());
      throw toCustomException(e);
    }
  }

  private CustomException toCustomException(RestClientException e) {
    if (e instanceof RestClientResponseException responseException) {
      if (responseException.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
        return new CustomException(ErrorCode.PREFERENCE_CATEGORY_MAPPING_NOT_FOUND);
      }
      if (responseException.getStatusCode().value() == HttpStatus.CONFLICT.value()) {
        return new CustomException(ErrorCode.PREFERENCE_CATEGORY_MAPPING_ALREADY_EXISTS);
      }
      if (responseException.getStatusCode().is4xxClientError()) {
        return new CustomException(ErrorCode.INVALID_REQUEST);
      }
    }
    return new CustomException(ErrorCode.AI_SERVER_UNAVAILABLE);
  }
}
