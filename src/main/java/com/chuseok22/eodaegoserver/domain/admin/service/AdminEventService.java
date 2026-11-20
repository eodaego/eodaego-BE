package com.chuseok22.eodaegoserver.domain.admin.service;

import com.chuseok22.eodaegoserver.domain.admin.CrawlJobType;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.AdminPageView;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.CrawlResultView;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.CulturalEventView;
import com.chuseok22.eodaegoserver.global.exception.CustomException;
import com.chuseok22.eodaegoserver.global.exception.ErrorCode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
public class AdminEventService {

  private static final Map<String, Comparator<CulturalEventView>> SORT_COMPARATORS = Map.of(
      "title", AdminSortResolver.caseInsensitive(CulturalEventView::title),
      "place", AdminSortResolver.caseInsensitive(CulturalEventView::place),
      "startDate", AdminSortResolver.nullsLast(CulturalEventView::startDate),
      "endDate", AdminSortResolver.nullsLast(CulturalEventView::endDate),
      "updatedAt", AdminSortResolver.nullsLast(CulturalEventView::updatedAt)
  );

  private final RestClient aiServerRestClient;
  private final CrawlExecutionLogService crawlExecutionLogService;

  public AdminPageView<CulturalEventView> searchEvents(String keyword, String sort, String direction, int page) {
    return AdminListSupport.paginate(
        listEvents(),
        AdminListSupport.keywordFilter(keyword, CulturalEventView::title, CulturalEventView::place),
        SORT_COMPARATORS, sort, direction, page);
  }

  private List<CulturalEventView> listEvents() {
    try {
      return aiServerRestClient.get()
          .uri("/api/v1/events")
          .retrieve()
          .body(new ParameterizedTypeReference<List<CulturalEventView>>() {});
    } catch (RestClientException e) {
      log.warn("[AdminEventService] 행사·공연 목록 조회 실패: {}", e.getMessage());
      throw new CustomException(ErrorCode.AI_SERVER_UNAVAILABLE);
    }
  }

  public CrawlResultView triggerEventCrawl() {
    try {
      CrawlResultView result = aiServerRestClient.post()
          .uri("/api/v1/events/crawl")
          .retrieve()
          .body(CrawlResultView.class);
      crawlExecutionLogService.record(CrawlJobType.EVENT_CRAWL, result.success(), result.collectedCount(), result.message());
      return result;
    } catch (RestClientResponseException e) {
      if (e.getStatusCode().value() == HttpStatus.CONFLICT.value()) {
        log.info("[AdminEventService] 이미 실행 중인 행사 크롤링 작업");
        crawlExecutionLogService.record(CrawlJobType.EVENT_CRAWL, false, 0, "이미 실행 중입니다");
        return new CrawlResultView(false, 0, "이미 실행 중입니다");
      }
      log.warn("[AdminEventService] 행사 크롤링 트리거 실패: {}", e.getMessage());
      crawlExecutionLogService.record(CrawlJobType.EVENT_CRAWL, false, 0, ErrorCode.AI_SERVER_UNAVAILABLE.getMessage());
      throw new CustomException(ErrorCode.AI_SERVER_UNAVAILABLE);
    } catch (RestClientException e) {
      log.warn("[AdminEventService] 행사 크롤링 트리거 실패: {}", e.getMessage());
      crawlExecutionLogService.record(CrawlJobType.EVENT_CRAWL, false, 0, ErrorCode.AI_SERVER_UNAVAILABLE.getMessage());
      throw new CustomException(ErrorCode.AI_SERVER_UNAVAILABLE);
    }
  }
}
