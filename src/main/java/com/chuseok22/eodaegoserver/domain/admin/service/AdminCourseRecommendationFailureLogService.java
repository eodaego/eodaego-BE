package com.chuseok22.eodaegoserver.domain.admin.service;

import com.chuseok22.eodaegoserver.domain.admin.dto.response.CourseRecommendationFailureLogView;
import com.chuseok22.eodaegoserver.domain.course.entity.CourseRecommendationFailureLog;
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
    Page<CourseRecommendationFailureLog> result = courseRecommendationFailureLogRepository.findAll(buildPageable(Math.max(page, 0)));
    // 마지막 페이지를 넘어선 요청은 빈 화면 대신 마지막 페이지를 보여 준다.
    if (page > 0 && result.getTotalPages() > 0 && page >= result.getTotalPages()) {
      result = courseRecommendationFailureLogRepository.findAll(buildPageable(result.getTotalPages() - 1));
    }
    log.debug("AI 코스 추천 실패 로그 목록 조회. page={}", page);
    return result.map(CourseRecommendationFailureLogView::from);
  }

  private Pageable buildPageable(int page) {
    return PageRequest.of(page, PAGE_SIZE,
        Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id")));
  }
}
