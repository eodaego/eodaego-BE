package com.chuseok22.eodaegoserver.domain.course.repository;

import com.chuseok22.eodaegoserver.domain.course.entity.CourseRecommendationFailureLog;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRecommendationFailureLogRepository extends JpaRepository<CourseRecommendationFailureLog, UUID> {

  long countByCreatedAtGreaterThanEqual(LocalDateTime threshold);

  List<CourseRecommendationFailureLog> findByCreatedAtGreaterThanEqual(LocalDateTime threshold);
}
