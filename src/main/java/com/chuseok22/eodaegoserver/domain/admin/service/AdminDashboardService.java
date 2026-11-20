package com.chuseok22.eodaegoserver.domain.admin.service;

import com.chuseok22.eodaegoserver.domain.admin.dto.response.CategoryCountView;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.DailyCountView;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.DashboardView;
import com.chuseok22.eodaegoserver.domain.ai.dto.response.AiHealthCheckResponse;
import com.chuseok22.eodaegoserver.domain.ai.service.AiHealthService;
import com.chuseok22.eodaegoserver.domain.catalog.CatalogCategory;
import com.chuseok22.eodaegoserver.domain.catalog.repository.CatalogItemRepository;
import com.chuseok22.eodaegoserver.domain.catalog.repository.CategoryCountProjection;
import com.chuseok22.eodaegoserver.domain.course.repository.CourseRecommendationFailureLogRepository;
import com.chuseok22.eodaegoserver.domain.member.repository.MemberRepository;
import com.chuseok22.eodaegoserver.global.exception.CustomException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminDashboardService {

  private static final int RECENT_DAYS = 7;

  // domain.admin이 다른 도메인의 Service 메서드를 재사용하지 않는다는 컨벤션의 취지는 "일반 사용자용 Service의
  // status 기준 필터링을 관리자가 그대로 물려받으면 안 된다"는 것이다. AiHealthService.checkHealth()는
  // 필터링이 전혀 없는 단순 상태 조회이므로 이 취지에 해당하지 않는다.
  private final AiHealthService aiHealthService;
  private final CourseRecommendationFailureLogRepository courseRecommendationFailureLogRepository;
  private final MemberRepository memberRepository;
  private final CatalogItemRepository catalogItemRepository;
  private final CrawlExecutionLogService crawlExecutionLogService;
  private final Clock clock;

  public AiHealthCheckResponse getAiHealthStatus() {
    try {
      return aiHealthService.checkHealth();
    } catch (CustomException e) {
      log.debug("[AdminDashboardService] AI 서버 헬스체크 DOWN 처리: {}", e.getMessage());
      return new AiHealthCheckResponse("DOWN", LocalDateTime.now(clock));
    }
  }

  public long countRecentRecommendationFailures() {
    return courseRecommendationFailureLogRepository.countByCreatedAtGreaterThanEqual(LocalDateTime.now(clock).minusHours(24));
  }

  public DashboardView getDashboard() {
    LocalDate today = LocalDate.now(clock);
    LocalDateTime recentStart = today.minusDays(RECENT_DAYS - 1L).atStartOfDay();
    List<DailyCountView> failureDailyCounts = buildFailureDailyCounts(today, recentStart);
    return new DashboardView(
        getAiHealthStatus(),
        memberRepository.count(),
        memberRepository.countByCreatedAtGreaterThanEqual(recentStart),
        buildCategoryCounts(),
        catalogItemRepository.countMissingChildDescription(),
        crawlExecutionLogService.listLatestPerJobType(),
        crawlExecutionLogService.retryableLogIds(),
        failureDailyCounts,
        failureDailyCounts.stream().mapToLong(DailyCountView::count).sum()
    );
  }

  private List<CategoryCountView> buildCategoryCounts() {
    Map<String, Long> countByCategoryName = catalogItemRepository.countGroupByCategory().stream()
        .collect(Collectors.toMap(CategoryCountProjection::getCategory, CategoryCountProjection::getCount));
    return Arrays.stream(CatalogCategory.values())
        .map(category -> new CategoryCountView(category, countByCategoryName.getOrDefault(category.name(), 0L)))
        .toList();
  }

  // 오늘을 포함한 최근 7일을 오래된 날부터 채우며, 실패가 없던 날도 0건으로 포함한다.
  private List<DailyCountView> buildFailureDailyCounts(LocalDate today, LocalDateTime recentStart) {
    Map<LocalDate, Long> countByDate = courseRecommendationFailureLogRepository.findByCreatedAtGreaterThanEqual(recentStart).stream()
        .collect(Collectors.groupingBy(failureLog -> failureLog.getCreatedAt().toLocalDate(), Collectors.counting()));
    return LongStream.range(0, RECENT_DAYS)
        .mapToObj(offset -> today.minusDays(RECENT_DAYS - 1 - offset))
        .map(date -> new DailyCountView(date, countByDate.getOrDefault(date, 0L)))
        .toList();
  }
}
