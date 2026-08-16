package com.chuseok22.eodaegoserver.domain.admin.dto.response;

import com.chuseok22.eodaegoserver.domain.course.CourseRecommendationFailureType;
import com.chuseok22.eodaegoserver.domain.course.entity.CourseRecommendationFailureLog;
import java.time.LocalDateTime;

public record CourseRecommendationFailureLogView(
    CourseRecommendationFailureType failureType,
    String message,
    LocalDateTime createdAt
) {

  public static CourseRecommendationFailureLogView from(CourseRecommendationFailureLog courseRecommendationFailureLog) {
    return new CourseRecommendationFailureLogView(
        courseRecommendationFailureLog.getFailureType(),
        courseRecommendationFailureLog.getMessage(),
        courseRecommendationFailureLog.getCreatedAt()
    );
  }
}
