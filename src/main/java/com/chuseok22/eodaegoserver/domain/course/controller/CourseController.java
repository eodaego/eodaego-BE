package com.chuseok22.eodaegoserver.domain.course.controller;

import com.chuseok22.eodaegoserver.domain.course.dto.request.CourseRecommendationRequest;
import com.chuseok22.eodaegoserver.domain.course.dto.response.CourseResponse;
import com.chuseok22.eodaegoserver.domain.course.service.CourseRecommendationService;
import com.chuseok22.logging.annotation.LogMonitoring;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController implements CourseControllerDocs {

  private final CourseRecommendationService courseRecommendationService;

  @Override
  @LogMonitoring
  @PostMapping(path = "/recommendations", version = "1")
  public ResponseEntity<List<CourseResponse>> recommendCourses(
      @Valid @RequestBody CourseRecommendationRequest request) {
    return ResponseEntity.ok(courseRecommendationService.recommendCourses(request));
  }

  @Override
  @LogMonitoring
  @GetMapping(path = "/{courseId}", version = "1")
  public ResponseEntity<CourseResponse> getCourse(
      @AuthenticationPrincipal UUID memberId,
      @PathVariable UUID courseId) {
    return ResponseEntity.ok(courseRecommendationService.getCourse(courseId, memberId));
  }

}
