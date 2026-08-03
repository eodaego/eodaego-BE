package com.chuseok22.eodaegoserver.domain.congestion.service;

import com.chuseok22.eodaegoserver.domain.congestion.dto.external.AiCongestionSnapshotResponse;
import com.chuseok22.eodaegoserver.global.exception.CustomException;
import com.chuseok22.eodaegoserver.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CongestionAiClient {

  private static final String CONGESTION_URI = "/api/v1/congestion";

  private final RestClient aiServerRestClient;

  public AiCongestionSnapshotResponse fetchLatestSnapshot() {
    try {
      List<AiCongestionSnapshotResponse> snapshots = aiServerRestClient.get()
        .uri(uriBuilder -> uriBuilder.path(CONGESTION_URI).queryParam("limit", 1).build())
        .retrieve()
        .body(new ParameterizedTypeReference<List<AiCongestionSnapshotResponse>>() {});

      if (snapshots == null || snapshots.isEmpty()) {
        log.error("AI 서버 혼잡도 응답이 비어 있습니다. uri={}", CONGESTION_URI);
        throw new CustomException(ErrorCode.AI_SERVER_UNAVAILABLE);
      }

      return snapshots.getFirst();
    } catch (RestClientException exception) {
      log.error("AI 서버 혼잡도 조회 실패. uri={}", CONGESTION_URI, exception);
      throw new CustomException(ErrorCode.AI_SERVER_UNAVAILABLE);
    }
  }
}
