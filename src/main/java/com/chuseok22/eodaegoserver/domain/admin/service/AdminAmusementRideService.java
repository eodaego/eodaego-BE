package com.chuseok22.eodaegoserver.domain.admin.service;

import com.chuseok22.eodaegoserver.domain.admin.dto.request.AmusementRideCreateRequest;
import com.chuseok22.eodaegoserver.domain.admin.dto.request.AmusementRideUpdateRequest;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.AmusementRideView;
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
public class AdminAmusementRideService {

  private static final String BASE_URI = "/api/v1/facility/amusement-rides";

  private final RestClient aiServerRestClient;

  public List<AmusementRideView> findAll() {
    try {
      return aiServerRestClient.get()
          .uri(BASE_URI)
          .retrieve()
          .body(new ParameterizedTypeReference<List<AmusementRideView>>() {});
    } catch (RestClientException e) {
      log.warn("[AdminAmusementRideService] 놀이기구 목록 조회 실패: {}", e.getMessage());
      throw new CustomException(ErrorCode.AI_SERVER_UNAVAILABLE);
    }
  }

  public AmusementRideView findById(Integer rideId) {
    return findAll().stream()
        .filter(ride -> ride.id().equals(rideId))
        .findFirst()
        .orElseThrow(() -> new CustomException(ErrorCode.AMUSEMENT_RIDE_NOT_FOUND));
  }

  public AmusementRideView create(AmusementRideCreateRequest request) {
    try {
      return aiServerRestClient.post()
          .uri(BASE_URI)
          .contentType(MediaType.APPLICATION_JSON)
          .body(request)
          .retrieve()
          .body(AmusementRideView.class);
    } catch (RestClientException e) {
      log.warn("[AdminAmusementRideService] 놀이기구 생성 실패: {}", e.getMessage());
      throw toCustomException(e);
    }
  }

  public AmusementRideView update(Integer rideId, AmusementRideUpdateRequest request) {
    try {
      return aiServerRestClient.patch()
          .uri(BASE_URI + "/{rideId}", rideId)
          .contentType(MediaType.APPLICATION_JSON)
          .body(request)
          .retrieve()
          .body(AmusementRideView.class);
    } catch (RestClientException e) {
      log.warn("[AdminAmusementRideService] 놀이기구 수정 실패: rideId={}, message={}", rideId, e.getMessage());
      throw toCustomException(e);
    }
  }

  public void delete(Integer rideId) {
    try {
      aiServerRestClient.delete()
          .uri(BASE_URI + "/{rideId}", rideId)
          .retrieve()
          .toBodilessEntity();
    } catch (RestClientException e) {
      log.warn("[AdminAmusementRideService] 놀이기구 삭제 실패: rideId={}, message={}", rideId, e.getMessage());
      throw toCustomException(e);
    }
  }

  private CustomException toCustomException(RestClientException e) {
    if (e instanceof RestClientResponseException responseException) {
      if (responseException.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
        return new CustomException(ErrorCode.AMUSEMENT_RIDE_NOT_FOUND);
      }
      if (responseException.getStatusCode().is4xxClientError()) {
        return new CustomException(ErrorCode.INVALID_REQUEST);
      }
    }
    return new CustomException(ErrorCode.AI_SERVER_UNAVAILABLE);
  }
}
