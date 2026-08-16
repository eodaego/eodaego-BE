package com.chuseok22.eodaegoserver.domain.course.repository;

import com.chuseok22.eodaegoserver.domain.course.entity.CourseRecommendationFailureLog;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRecommendationFailureLogRepository extends JpaRepository<CourseRecommendationFailureLog, UUID> {
}
