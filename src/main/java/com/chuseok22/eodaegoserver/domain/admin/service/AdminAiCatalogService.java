package com.chuseok22.eodaegoserver.domain.admin.service;

import com.chuseok22.eodaegoserver.domain.admin.CrawlJobType;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.CatalogCrawlResultView;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.CongestionView;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.CrawlResultView;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.OperatingHoursView;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.WeatherSnapshotView;
import com.chuseok22.eodaegoserver.global.exception.CustomException;
import com.chuseok22.eodaegoserver.global.exception.ErrorCode;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminAiCatalogService {

  private final RestClient aiServerRestClient;
  private final CrawlExecutionLogService crawlExecutionLogService;

  public List<OperatingHoursView> listOperatingHours() {
    return get("/api/v1/facility/operating-hours", new ParameterizedTypeReference<List<OperatingHoursView>>() {});
  }

  public List<CongestionView> listCongestion() {
    return get("/api/v1/congestion", new ParameterizedTypeReference<List<CongestionView>>() {});
  }

  public List<WeatherSnapshotView> listWeather() {
    return get("/api/v1/weather", new ParameterizedTypeReference<List<WeatherSnapshotView>>() {});
  }

  public CatalogCrawlResultView triggerCatalogCrawl() {
    try {
      CatalogCrawlResultView result = aiServerRestClient.post()
          .uri("/api/v1/catalog/crawl")
          .retrieve()
          .body(CatalogCrawlResultView.class);
      recordCatalogCrawl(result);
      return result;
    } catch (RestClientResponseException e) {
      if (e.getStatusCode().value() == HttpStatus.CONFLICT.value()) {
        log.info("[AdminAiCatalogService] 이미 실행 중인 도감 크롤링 작업");
        CrawlResultView running = new CrawlResultView(false, 0, "이미 실행 중입니다");
        CatalogCrawlResultView result = new CatalogCrawlResultView(running, running, running);
        recordCatalogCrawl(result);
        return result;
      }
      log.warn("[AdminAiCatalogService] 도감 크롤링 트리거 실패: {}", e.getMessage());
      crawlExecutionLogService.record(CrawlJobType.CATALOG_CRAWL, false, 0, ErrorCode.AI_SERVER_UNAVAILABLE.getMessage());
      throw new CustomException(ErrorCode.AI_SERVER_UNAVAILABLE);
    } catch (RestClientException e) {
      log.warn("[AdminAiCatalogService] 도감 크롤링 트리거 실패: {}", e.getMessage());
      crawlExecutionLogService.record(CrawlJobType.CATALOG_CRAWL, false, 0, ErrorCode.AI_SERVER_UNAVAILABLE.getMessage());
      throw new CustomException(ErrorCode.AI_SERVER_UNAVAILABLE);
    }
  }

  private void recordCatalogCrawl(CatalogCrawlResultView result) {
    boolean success = result.animals().success() && result.plants().success() && result.locations().success();
    int totalCount = safeCount(result.animals()) + safeCount(result.plants()) + safeCount(result.locations());
    String message = describeCrawlResult("동물", result.animals()) + ", "
        + describeCrawlResult("식물", result.plants()) + ", "
        + describeCrawlResult("위치", result.locations());
    crawlExecutionLogService.record(CrawlJobType.CATALOG_CRAWL, success, totalCount, message);
  }

  private int safeCount(CrawlResultView result) {
    return Objects.requireNonNullElse(result.collectedCount(), 0);
  }

  private String describeCrawlResult(String label, CrawlResultView result) {
    return result.success() ? label + " " + safeCount(result) + "건" : label + ": " + result.message();
  }

  public CrawlResultView triggerOperatingHoursCrawl() {
    return postCrawl("/api/v1/facility/operating-hours/crawl", CrawlJobType.OPERATING_HOURS_CRAWL);
  }

  public CrawlResultView triggerWeatherCrawl() {
    return postCrawl("/api/v1/weather/crawl", CrawlJobType.WEATHER_CRAWL);
  }

  public CrawlResultView triggerCongestionCrawl() {
    return postCrawl("/api/v1/congestion/crawl", CrawlJobType.CONGESTION_CRAWL);
  }

  private CrawlResultView postCrawl(String uri, CrawlJobType jobType) {
    try {
      CrawlResultView result = aiServerRestClient.post()
          .uri(uri)
          .retrieve()
          .body(CrawlResultView.class);
      crawlExecutionLogService.record(jobType, result.success(), result.collectedCount(), result.message());
      return result;
    } catch (RestClientResponseException e) {
      if (e.getStatusCode().value() == HttpStatus.CONFLICT.value()) {
        log.info("[AdminAiCatalogService] 이미 실행 중인 크롤링 작업: uri={}", uri);
        crawlExecutionLogService.record(jobType, false, 0, "이미 실행 중입니다");
        return new CrawlResultView(false, 0, "이미 실행 중입니다");
      }
      log.warn("[AdminAiCatalogService] 크롤링 트리거 실패: uri={}, message={}", uri, e.getMessage());
      crawlExecutionLogService.record(jobType, false, 0, ErrorCode.AI_SERVER_UNAVAILABLE.getMessage());
      throw new CustomException(ErrorCode.AI_SERVER_UNAVAILABLE);
    } catch (RestClientException e) {
      log.warn("[AdminAiCatalogService] 크롤링 트리거 실패: uri={}, message={}", uri, e.getMessage());
      crawlExecutionLogService.record(jobType, false, 0, ErrorCode.AI_SERVER_UNAVAILABLE.getMessage());
      throw new CustomException(ErrorCode.AI_SERVER_UNAVAILABLE);
    }
  }

  private <T> List<T> get(String uri, ParameterizedTypeReference<List<T>> responseType) {
    try {
      return aiServerRestClient.get()
          .uri(uri)
          .retrieve()
          .body(responseType);
    } catch (RestClientException e) {
      log.warn("[AdminAiCatalogService] AI 서버 조회 실패: uri={}, message={}", uri, e.getMessage());
      throw new CustomException(ErrorCode.AI_SERVER_UNAVAILABLE);
    }
  }
}
