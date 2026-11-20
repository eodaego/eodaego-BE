package com.chuseok22.eodaegoserver.domain.admin.service;

import com.chuseok22.eodaegoserver.domain.admin.CrawlJobType;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.AdminActionResult;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.CatalogCrawlResultView;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.CrawlResultView;
import com.chuseok22.eodaegoserver.global.exception.CustomException;
import com.chuseok22.eodaegoserver.global.exception.ErrorCode;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminCrawlRetryService {

  private final CrawlExecutionLogService crawlExecutionLogService;
  private final AdminCatalogItemService adminCatalogItemService;
  private final AdminFacilityService adminFacilityService;
  private final AdminEventService adminEventService;
  private final AdminAiCatalogService adminAiCatalogService;

  // 각 트리거가 실행 이력을 직접 기록하므로 여기서는 기록하지 않는다.
  public AdminActionResult retry(String jobTypeName) {
    CrawlJobType jobType = parseJobType(jobTypeName);
    if (jobType == null || !crawlExecutionLogService.isRetryable(jobType)) {
      log.warn("크롤링 재실행 거부. jobType={}", jobTypeName);
      return new AdminActionResult(false, ErrorCode.CRAWL_RETRY_NOT_ALLOWED.getMessage());
    }
    try {
      CrawlResultView result = trigger(jobType);
      log.info("크롤링 재실행 완료. jobType={}, success={}", jobType, result.success());
      return toActionResult(jobType, result);
    } catch (CustomException e) {
      log.warn("크롤링 재실행 실패. jobType={}, message={}", jobType, e.getMessage());
      return new AdminActionResult(false, e.getMessage());
    }
  }

  private CrawlJobType parseJobType(String jobTypeName) {
    try {
      return CrawlJobType.valueOf(jobTypeName);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  private CrawlResultView trigger(CrawlJobType jobType) {
    return switch (jobType) {
      case CATALOG_SYNC -> adminCatalogItemService.triggerSync();
      case FACILITY_IMPORT -> adminFacilityService.triggerFacilityImport();
      case EVENT_CRAWL -> adminEventService.triggerEventCrawl();
      case CATALOG_CRAWL -> merge(adminAiCatalogService.triggerCatalogCrawl());
      case OPERATING_HOURS_CRAWL -> adminAiCatalogService.triggerOperatingHoursCrawl();
      case WEATHER_CRAWL -> adminAiCatalogService.triggerWeatherCrawl();
      case CONGESTION_CRAWL -> adminAiCatalogService.triggerCongestionCrawl();
    };
  }

  private CrawlResultView merge(CatalogCrawlResultView result) {
    List<CrawlResultView> parts = List.of(result.animals(), result.plants(), result.locations());
    int totalCount = parts.stream().mapToInt(part -> Objects.requireNonNullElse(part.collectedCount(), 0)).sum();
    return parts.stream()
        .filter(part -> !part.success())
        .findFirst()
        .map(failure -> new CrawlResultView(false, totalCount, failure.message()))
        .orElseGet(() -> new CrawlResultView(true, totalCount, "도감 크롤링 완료"));
  }

  private AdminActionResult toActionResult(CrawlJobType jobType, CrawlResultView result) {
    if (!result.success()) {
      return new AdminActionResult(false, jobType + " 재실행 실패: " + result.message());
    }
    int collectedCount = Objects.requireNonNullElse(result.collectedCount(), 0);
    return new AdminActionResult(true, jobType + " 재실행 완료. 수집 " + collectedCount + "건");
  }
}
