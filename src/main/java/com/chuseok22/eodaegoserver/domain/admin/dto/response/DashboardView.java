package com.chuseok22.eodaegoserver.domain.admin.dto.response;

import com.chuseok22.eodaegoserver.domain.ai.dto.response.AiHealthCheckResponse;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record DashboardView(
    AiHealthCheckResponse aiHealth,
    long memberTotalCount,
    long memberNewLastSevenDaysCount,
    List<CategoryCountView> catalogCategoryCounts,
    long catalogMissingChildDescriptionCount,
    List<CrawlExecutionLogView> latestCrawlLogs,
    Set<UUID> retryableLogIds,
    List<DailyCountView> recommendationFailureDailyCounts,
    long recommendationFailureLastSevenDaysCount
) {

  public long catalogTotalCount() {
    return catalogCategoryCounts.stream().mapToLong(CategoryCountView::count).sum();
  }

  // 도감이 비어 있으면 0으로 나누지 않고 0%로 표시한다.
  public int childDescriptionCompletionPercent() {
    long totalCount = catalogTotalCount();
    if (totalCount == 0) {
      return 0;
    }
    return (int) ((totalCount - catalogMissingChildDescriptionCount) * 100 / totalCount);
  }
}
