package com.chuseok22.eodaegoserver.domain.admin.dto.response;

import com.chuseok22.eodaegoserver.domain.admin.CrawlJobType;
import com.chuseok22.eodaegoserver.domain.admin.entity.CrawlExecutionLog;
import java.time.LocalDateTime;

public record CrawlExecutionLogView(
    CrawlJobType jobType,
    boolean success,
    int collectedCount,
    String message,
    LocalDateTime createdAt
) {

  public static CrawlExecutionLogView from(CrawlExecutionLog crawlExecutionLog) {
    return new CrawlExecutionLogView(
        crawlExecutionLog.getJobType(),
        crawlExecutionLog.isSuccess(),
        crawlExecutionLog.getCollectedCount(),
        crawlExecutionLog.getMessage(),
        crawlExecutionLog.getCreatedAt()
    );
  }
}
