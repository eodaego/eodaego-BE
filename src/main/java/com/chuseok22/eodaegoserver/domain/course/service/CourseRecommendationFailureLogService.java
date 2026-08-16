package com.chuseok22.eodaegoserver.domain.course.service;

import com.chuseok22.eodaegoserver.domain.course.CourseRecommendationFailureType;
import com.chuseok22.eodaegoserver.domain.course.entity.CourseRecommendationFailureLog;
import com.chuseok22.eodaegoserver.domain.course.repository.CourseRecommendationFailureLogRepository;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseRecommendationFailureLogService {

  private static final int MESSAGE_MAX_LENGTH = 1000;

  private final CourseRecommendationFailureLogRepository courseRecommendationFailureLogRepository;
  private final PlatformTransactionManager transactionManager;

  public void record(CourseRecommendationFailureType failureType, String message) {
    String normalizedMessage = Objects.requireNonNullElse(message, "");
    if (normalizedMessage.length() > MESSAGE_MAX_LENGTH) {
      normalizedMessage = normalizedMessage.substring(0, MESSAGE_MAX_LENGTH);
    }

    CourseRecommendationFailureLog courseRecommendationFailureLog = CourseRecommendationFailureLog.builder()
        .failureType(failureType)
        .message(normalizedMessage)
        .build();
    try {
      TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
      transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
      transactionTemplate.executeWithoutResult(
          status -> courseRecommendationFailureLogRepository.save(courseRecommendationFailureLog));
      log.debug("AI 코스 추천 실패 로그 저장. failureType={}", failureType);
    } catch (Exception e) {
      log.error("AI 코스 추천 실패 로그 저장 실패. failureType={}", failureType, e);
    }
  }
}
