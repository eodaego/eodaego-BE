package com.chuseok22.eodaegoserver.domain.ai.controller;

import com.chuseok22.eodaegoserver.domain.ai.dto.response.AiHealthCheckResponse;
import com.chuseok22.eodaegoserver.domain.ai.service.AiHealthService;
import com.chuseok22.logging.annotation.LogMonitoring;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiHealthController implements AiHealthControllerDocs {

  private final AiHealthService aiHealthService;

  @Override
  @LogMonitoring
  @GetMapping(path = "/health", version = "1")
  public ResponseEntity<AiHealthCheckResponse> checkAiServerHealth() {
    return ResponseEntity.ok(aiHealthService.checkHealth());
  }
}
