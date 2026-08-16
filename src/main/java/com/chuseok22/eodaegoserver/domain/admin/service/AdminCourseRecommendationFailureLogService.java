package com.chuseok22.eodaegoserver.domain.admin.service;

import com.chuseok22.eodaegoserver.domain.admin.dto.response.CourseRecommendationFailureLogView;
import com.chuseok22.eodaegoserver.domain.course.repository.CourseRecommendationFailureLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminCourseRecommendationFailureLogService {

  private static final int PAGE_SIZE = 20;

  private final CourseRecommendationFailureLogRepository courseRecommendationFailureLogRepository;

  public Page<CourseRecommendationFailureLogView> listLogs(int page) {
    Pageable pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
    return courseRecommendationFailureLogRepository.findAll(pageable).map(CourseRecommendationFailureLogView::from);
  }
}
