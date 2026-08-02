package com.chuseok22.eodaegoserver.domain.course.service;

import com.chuseok22.eodaegoserver.domain.course.dto.external.AiRecommendedCourse;
import com.chuseok22.eodaegoserver.domain.course.dto.external.AiRouteRecommendationRequest;
import com.chuseok22.eodaegoserver.domain.course.dto.external.AiRouteRecommendationResponse;
import com.chuseok22.eodaegoserver.domain.course.dto.external.AiRouteStop;
import com.chuseok22.eodaegoserver.global.exception.CustomException;
import com.chuseok22.eodaegoserver.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourseAiClient {

  private final RestClient aiServerRestClient;

  public AiRouteRecommendationResponse recommendRoutes(AiRouteRecommendationRequest request) {
    AiRouteRecommendationResponse response;
    try {
      response = aiServerRestClient.post()
          .uri("/api/v1/recommendation/routes")
          .body(request)
          .retrieve()
          .body(AiRouteRecommendationResponse.class);
    } catch (RestClientException exception) {
      log.error("AI 서버 코스 추천 호출 실패", exception);
      throw new CustomException(ErrorCode.AI_SERVER_UNAVAILABLE);
    }

    validateResponse(response);
    return response;
  }

  private void validateResponse(AiRouteRecommendationResponse response) {
    if (response == null || response.courses() == null || response.courses().isEmpty()) {
      log.warn("AI 추천 응답에 코스가 없습니다.");
      throw new CustomException(ErrorCode.AI_SERVER_UNAVAILABLE);
    }

    for (AiRecommendedCourse course : response.courses()) {
      if (course.title() == null
          || course.tagLabels() == null
          || course.estimatedDurationMinutes() == null
          || course.stops() == null) {
        log.warn("AI 코스 필수값 누락. title={}", course.title());
        throw new CustomException(ErrorCode.AI_SERVER_UNAVAILABLE);
      }

      for (AiRouteStop stop : course.stops()) {
        if (stop.facility() == null || stop.facility().id() == null) {
          log.warn("AI 코스 시설정보 누락. title={}", course.title());
          throw new CustomException(ErrorCode.AI_SERVER_UNAVAILABLE);
        }
      }
    }
  }

}
