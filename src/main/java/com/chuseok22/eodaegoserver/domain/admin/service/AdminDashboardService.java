package com.chuseok22.eodaegoserver.domain.admin.service;

import com.chuseok22.eodaegoserver.domain.ai.dto.response.AiHealthCheckResponse;
import com.chuseok22.eodaegoserver.domain.ai.service.AiHealthService;
import com.chuseok22.eodaegoserver.global.exception.CustomException;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminDashboardService {

  // domain.admin이 다른 도메인의 Service 메서드를 재사용하지 않는다는 컨벤션의 취지는 "일반 사용자용 Service의
  // status 기준 필터링을 관리자가 그대로 물려받으면 안 된다"는 것이다. AiHealthService.checkHealth()는
  // 필터링이 전혀 없는 단순 상태 조회이므로 이 취지에 해당하지 않는다.
  private final AiHealthService aiHealthService;
  private final Clock clock;

  public AiHealthCheckResponse getAiHealthStatus() {
    try {
      return aiHealthService.checkHealth();
    } catch (CustomException e) {
      log.debug("[AdminDashboardService] AI 서버 헬스체크 DOWN 처리: {}", e.getMessage());
      return new AiHealthCheckResponse("DOWN", LocalDateTime.now(clock));
    }
  }
}
