package com.chuseok22.eodaegoserver.domain.congestion.controller;

import com.chuseok22.eodaegoserver.domain.congestion.dto.response.CongestionResponse;
import com.chuseok22.eodaegoserver.domain.congestion.service.CongestionService;
import com.chuseok22.logging.annotation.LogMonitoring;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/congestion")
@RequiredArgsConstructor
public class CongestionController implements CongestionControllerDocs {

  private final CongestionService congestionService;

  @Override
  @LogMonitoring
  @GetMapping(path = "/current", version = "1")
  public ResponseEntity<CongestionResponse> getCurrentCongestion() {
    return ResponseEntity.ok(congestionService.getCurrentCongestion());
  }
}
