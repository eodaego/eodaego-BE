package com.chuseok22.eodaegoserver.domain.admin.service;

import com.chuseok22.eodaegoserver.domain.admin.CrawlJobType;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.CrawlExecutionLogView;
import com.chuseok22.eodaegoserver.domain.admin.entity.CrawlExecutionLog;
import com.chuseok22.eodaegoserver.domain.admin.repository.CrawlExecutionLogRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
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
    Page<CrawlExecutionLog> result = crawlExecutionLogRepository.findAll(buildPageable(Math.max(page, 0)));
    // 마지막 페이지를 넘어선 요청은 빈 화면 대신 마지막 페이지를 보여 준다.
    if (page > 0 && result.getTotalPages() > 0 && page >= result.getTotalPages()) {
      result = crawlExecutionLogRepository.findAll(buildPageable(result.getTotalPages() - 1));
    }
    return result.map(CrawlExecutionLogView::from);
  }

  public List<CrawlExecutionLogView> listLatestPerJobType() {
    return Arrays.stream(CrawlJobType.values())
        .map(crawlExecutionLogRepository::findTopByJobTypeOrderByCreatedAtDesc)
        .flatMap(Optional::stream)
        .map(CrawlExecutionLogView::from)
        .toList();
  }

  public boolean isRetryable(CrawlJobType jobType) {
    return crawlExecutionLogRepository.findTopByJobTypeOrderByCreatedAtDesc(jobType)
        .map(latest -> !latest.isSuccess())
        .orElse(false);
  }

  public Set<UUID> retryableLogIds() {
    return listLatestPerJobType().stream()
        .filter(latest -> !latest.success())
        .map(CrawlExecutionLogView::id)
        .collect(Collectors.toSet());
  }

  private Pageable buildPageable(int page) {
    return PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
  }
}
