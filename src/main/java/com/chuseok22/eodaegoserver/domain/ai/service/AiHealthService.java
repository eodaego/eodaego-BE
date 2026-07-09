package com.chuseok22.eodaegoserver.domain.ai.service;

import com.chuseok22.eodaegoserver.domain.ai.dto.response.AiHealthCheckResponse;
import com.chuseok22.eodaegoserver.global.exception.CustomException;
import com.chuseok22.eodaegoserver.global.exception.ErrorCode;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiHealthService {

  private final RestClient aiServerRestClient;
  private final Clock clock;

  public AiHealthCheckResponse checkHealth() {
    try {
      aiServerRestClient.get()
          .uri("/health")
          .retrieve()
          .toBodilessEntity();

      return new AiHealthCheckResponse("UP", LocalDateTime.now(clock));
    } catch (RestClientException e) {
      // 타임아웃, 커넥션 거부, AI 서버의 4xx/5xx 응답을 모두 포함해 연결 실패로 취급한다.
      log.warn("[AiHealthService] AI 서버 헬스체크 실패: {}", e.getMessage());
      throw new CustomException(ErrorCode.AI_SERVER_UNAVAILABLE);
    }
  }
}
