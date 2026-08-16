package com.chuseok22.eodaegoserver.domain.admin.service;

import com.chuseok22.eodaegoserver.domain.admin.CrawlJobType;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.CrawlExecutionLogView;
import com.chuseok22.eodaegoserver.domain.admin.entity.CrawlExecutionLog;
import com.chuseok22.eodaegoserver.domain.admin.repository.CrawlExecutionLogRepository;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlExecutionLogService {

  private static final int PAGE_SIZE = 20;
  private static final int MESSAGE_MAX_LENGTH = 1000;

  private final CrawlExecutionLogRepository crawlExecutionLogRepository;

  public void record(CrawlJobType jobType, boolean success, Integer collectedCount, String message) {
    String normalizedMessage = Objects.requireNonNullElse(message, "");
    if (normalizedMessage.length() > MESSAGE_MAX_LENGTH) {
      normalizedMessage = normalizedMessage.substring(0, MESSAGE_MAX_LENGTH);
    }
    CrawlExecutionLog crawlExecutionLog = CrawlExecutionLog.builder()
        .jobType(jobType)
        .success(success)
        .collectedCount(Objects.requireNonNullElse(collectedCount, 0))
        .message(normalizedMessage)
        .build();
    try {
      crawlExecutionLogRepository.save(crawlExecutionLog);
      log.debug("크롤링 실행 이력 저장. jobType={}, success={}, collectedCount={}", jobType, success, collectedCount);
    } catch (Exception e) {
      log.error("크롤링 실행 이력 저장 실패. jobType={}, success={}", jobType, success, e);
    }
  }

  public Page<CrawlExecutionLogView> listLogs(int page) {
    Pageable pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
    return crawlExecutionLogRepository.findAll(pageable).map(CrawlExecutionLogView::from);
  }
}
