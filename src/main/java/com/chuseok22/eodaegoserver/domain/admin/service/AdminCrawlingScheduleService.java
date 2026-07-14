package com.chuseok22.eodaegoserver.domain.admin.service;

import com.chuseok22.eodaegoserver.domain.admin.dto.request.CrawlingScheduleCreateRequest;
import com.chuseok22.eodaegoserver.domain.admin.dto.request.CrawlingScheduleUpdateRequest;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.CrawlingScheduleView;
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
public class AdminCrawlingScheduleService {

  private static final String BASE_URI = "/api/v1/crawling/schedules";

  private final RestClient aiServerRestClient;

  public List<CrawlingScheduleView> findAll() {
    try {
      return aiServerRestClient.get()
          .uri(BASE_URI)
          .retrieve()
          .body(new ParameterizedTypeReference<List<CrawlingScheduleView>>() {});
    } catch (RestClientException e) {
      log.warn("[AdminCrawlingScheduleService] 크롤링 스케줄 목록 조회 실패: {}", e.getMessage());
      throw new CustomException(ErrorCode.AI_SERVER_UNAVAILABLE);
    }
  }

  public CrawlingScheduleView findById(Integer scheduleId) {
    return findAll().stream()
        .filter(schedule -> schedule.id().equals(scheduleId))
        .findFirst()
        .orElseThrow(() -> new CustomException(ErrorCode.CRAWLING_SCHEDULE_NOT_FOUND));
  }

  public CrawlingScheduleView create(CrawlingScheduleCreateRequest request) {
    try {
      return aiServerRestClient.post()
          .uri(BASE_URI)
          .contentType(MediaType.APPLICATION_JSON)
          .body(request)
          .retrieve()
          .body(CrawlingScheduleView.class);
    } catch (RestClientException e) {
      log.warn("[AdminCrawlingScheduleService] 크롤링 스케줄 생성 실패: {}", e.getMessage());
      throw toCustomException(e);
    }
  }

  public CrawlingScheduleView update(Integer scheduleId, CrawlingScheduleUpdateRequest request) {
    try {
      return aiServerRestClient.patch()
          .uri(BASE_URI + "/{scheduleId}", scheduleId)
          .contentType(MediaType.APPLICATION_JSON)
          .body(request)
          .retrieve()
          .body(CrawlingScheduleView.class);
    } catch (RestClientException e) {
      log.warn("[AdminCrawlingScheduleService] 크롤링 스케줄 수정 실패: scheduleId={}, message={}", scheduleId, e.getMessage());
      throw toCustomException(e);
    }
  }

  public void delete(Integer scheduleId) {
    try {
      aiServerRestClient.delete()
          .uri(BASE_URI + "/{scheduleId}", scheduleId)
          .retrieve()
          .toBodilessEntity();
    } catch (RestClientException e) {
      log.warn("[AdminCrawlingScheduleService] 크롤링 스케줄 삭제 실패: scheduleId={}, message={}", scheduleId, e.getMessage());
      throw toCustomException(e);
    }
  }

  private CustomException toCustomException(RestClientException e) {
    if (e instanceof RestClientResponseException responseException) {
      if (responseException.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
        return new CustomException(ErrorCode.CRAWLING_SCHEDULE_NOT_FOUND);
      }
      if (responseException.getStatusCode().is4xxClientError()) {
        return new CustomException(ErrorCode.INVALID_REQUEST);
      }
    }
    return new CustomException(ErrorCode.AI_SERVER_UNAVAILABLE);
  }
}
