package com.chuseok22.eodaegoserver.domain.congestion.service;

import com.chuseok22.eodaegoserver.domain.congestion.dto.response.CongestionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CongestionService {

  private final CongestionAiClient congestionAiClient;

  public CongestionResponse getCurrentCongestion() {
    return CongestionResponse.from(congestionAiClient.fetchLatestSnapshot());
  }
}
