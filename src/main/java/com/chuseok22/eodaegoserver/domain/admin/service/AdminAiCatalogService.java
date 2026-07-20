package com.chuseok22.eodaegoserver.domain.admin.service;

import com.chuseok22.eodaegoserver.domain.admin.dto.response.AnimalView;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.CatalogCrawlResultView;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.CongestionView;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.CrawlResultView;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.FacilityView;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.OperatingHoursView;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.PlantView;
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

  public List<FacilityView> listFacilities() {
    return get("/api/v1/facility", new ParameterizedTypeReference<List<FacilityView>>() {});
  }

  public List<OperatingHoursView> listOperatingHours() {
    return get("/api/v1/facility/operating-hours", new ParameterizedTypeReference<List<OperatingHoursView>>() {});
  }

  public List<AnimalView> listAnimals() {
    return get("/api/v1/catalog/animals", new ParameterizedTypeReference<List<AnimalView>>() {});
  }

  public List<PlantView> listPlants() {
    return get("/api/v1/catalog/plants", new ParameterizedTypeReference<List<PlantView>>() {});
  }

  public List<CongestionView> listCongestion() {
    return get("/api/v1/congestion", new ParameterizedTypeReference<List<CongestionView>>() {});
  }

  public List<WeatherSnapshotView> listWeather() {
    return get("/api/v1/weather", new ParameterizedTypeReference<List<WeatherSnapshotView>>() {});
  }

  public List<String> listFacilityCategories() {
    return listFacilities().stream()
        .map(FacilityView::category)
        .filter(Objects::nonNull)
        .distinct()
        .sorted()
        .toList();
  }

  public CatalogCrawlResultView triggerCatalogCrawl() {
    try {
      return aiServerRestClient.post()
          .uri("/api/v1/catalog/crawl")
          .retrieve()
          .body(CatalogCrawlResultView.class);
    } catch (RestClientResponseException e) {
      if (e.getStatusCode().value() == HttpStatus.CONFLICT.value()) {
        log.info("[AdminAiCatalogService] 이미 실행 중인 도감 크롤링 작업");
        CrawlResultView running = new CrawlResultView(false, 0, "이미 실행 중입니다");
        return new CatalogCrawlResultView(running, running, running);
      }
      log.warn("[AdminAiCatalogService] 도감 크롤링 트리거 실패: {}", e.getMessage());
      throw new CustomException(ErrorCode.AI_SERVER_UNAVAILABLE);
    } catch (RestClientException e) {
      log.warn("[AdminAiCatalogService] 도감 크롤링 트리거 실패: {}", e.getMessage());
      throw new CustomException(ErrorCode.AI_SERVER_UNAVAILABLE);
    }
  }

  public CrawlResultView triggerOperatingHoursCrawl() {
    return postCrawl("/api/v1/facility/operating-hours/crawl");
  }

  public CrawlResultView triggerFacilityImport() {
    return postCrawl("/api/v1/facility/import");
  }

  public CrawlResultView triggerWeatherCrawl() {
    return postCrawl("/api/v1/weather/crawl");
  }

  private CrawlResultView postCrawl(String uri) {
    try {
      return aiServerRestClient.post()
          .uri(uri)
          .retrieve()
          .body(CrawlResultView.class);
    } catch (RestClientResponseException e) {
      if (e.getStatusCode().value() == HttpStatus.CONFLICT.value()) {
        log.info("[AdminAiCatalogService] 이미 실행 중인 크롤링 작업: uri={}", uri);
        return new CrawlResultView(false, 0, "이미 실행 중입니다");
      }
      log.warn("[AdminAiCatalogService] 크롤링 트리거 실패: uri={}, message={}", uri, e.getMessage());
      throw new CustomException(ErrorCode.AI_SERVER_UNAVAILABLE);
    } catch (RestClientException e) {
      log.warn("[AdminAiCatalogService] 크롤링 트리거 실패: uri={}, message={}", uri, e.getMessage());
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
