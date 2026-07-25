package com.chuseok22.eodaegoserver.domain.course.service;

import com.chuseok22.eodaegoserver.domain.course.dto.external.AiRouteRecommendationRequest;
import com.chuseok22.eodaegoserver.domain.course.dto.external.AiRouteRecommendationResponse;
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
    try {
      return aiServerRestClient.post()
          .uri("/api/v1/recommendation/routes")
          .body(request)
          .retrieve()
          .body(AiRouteRecommendationResponse.class);
    } catch (RestClientException exception) {
      log.error("AI 서버 코스 추천 호출 실패", exception);
      throw new CustomException(ErrorCode.AI_SERVER_UNAVAILABLE);
    }
  }

}
