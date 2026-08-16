package com.chuseok22.eodaegoserver.domain.admin.service;

import com.chuseok22.eodaegoserver.domain.admin.dto.response.CourseRecommendationFailureLogView;
import com.chuseok22.eodaegoserver.domain.course.repository.CourseRecommendationFailureLogRepository;
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
public class AdminCourseRecommendationFailureLogService {

  private static final int PAGE_SIZE = 20;

  private final CourseRecommendationFailureLogRepository courseRecommendationFailureLogRepository;

  public Page<CourseRecommendationFailureLogView> listLogs(int page) {
    Pageable pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE,
        Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id")));
    log.debug("AI 코스 추천 실패 로그 목록 조회. page={}", page);
    return courseRecommendationFailureLogRepository.findAll(pageable).map(CourseRecommendationFailureLogView::from);
  }
}
