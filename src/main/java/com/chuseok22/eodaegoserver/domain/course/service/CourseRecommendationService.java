package com.chuseok22.eodaegoserver.domain.course.service;

import com.chuseok22.eodaegoserver.domain.course.CompanionType;
import com.chuseok22.eodaegoserver.domain.course.EntranceGate;
import com.chuseok22.eodaegoserver.domain.course.InterestType;
import com.chuseok22.eodaegoserver.domain.course.dto.external.AiRecommendedCourse;
import com.chuseok22.eodaegoserver.domain.course.dto.external.AiRouteRecommendationRequest;
import com.chuseok22.eodaegoserver.domain.course.dto.external.AiRouteRecommendationResponse;
import com.chuseok22.eodaegoserver.domain.course.dto.request.CourseRecommendationRequest;
import com.chuseok22.eodaegoserver.domain.course.dto.response.CourseResponse;
import com.chuseok22.eodaegoserver.domain.course.entity.Course;
import com.chuseok22.eodaegoserver.domain.course.entity.CoursePlace;
import com.chuseok22.eodaegoserver.domain.course.repository.CourseFavoriteRepository;
import com.chuseok22.eodaegoserver.domain.course.repository.CourseRepository;
import com.chuseok22.eodaegoserver.global.exception.CustomException;
import com.chuseok22.eodaegoserver.global.exception.ErrorCode;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CourseRecommendationService {

  private static final int DEFAULT_STAY_DURATION_MINUTES = 1440;
  private static final CompanionType DEFAULT_COMPANION_TYPE = CompanionType.ALONE;

  private final CourseRepository courseRepository;
  private final CourseFavoriteRepository courseFavoriteRepository;
  private final CourseAiClient courseAiClient;

  @Transactional
  public List<CourseResponse> recommendCourses(CourseRecommendationRequest request) {

    List<InterestType> preferenceTags = resolvePreferenceTags(request.preferenceTags());
    int stayDurationMinutes = resolveStayDurationMinutes(request.stayDurationMinutes());
    CompanionType companionType = resolveCompanionType(request.companionType());

    AiRouteRecommendationRequest aiRequest = new AiRouteRecommendationRequest(
        preferenceTags,
        stayDurationMinutes,
        request.entrance(),
        request.exit(),
        companionType
    );

    AiRouteRecommendationResponse aiResponse = courseAiClient.recommendRoutes(aiRequest);

    List<Course> savedCourses = aiResponse.courses().stream()
        .map(aiCourse -> toCourse(aiCourse, preferenceTags.get(0), stayDurationMinutes, request.entrance(), request.exit()))
        .map(courseRepository::save)
        .toList();

    log.info("코스 추천 완료. 추천된 코스 수={}", savedCourses.size());

    return savedCourses.stream()
        .map(course -> CourseResponse.from(course, false))
        .toList();

  }

  public CourseResponse getCourse(UUID courseId, UUID memberId) {
    Course course = courseRepository.findById(courseId)
        .orElseThrow(() -> {
          log.warn("코스 조회 실패. courseId={}", courseId);
          throw new CustomException(ErrorCode.COURSE_NOT_FOUND);
        });

    boolean favorite = courseFavoriteRepository.existsByMemberIdAndCourseId(memberId, courseId);

    return CourseResponse.from(course, favorite);
  }

  private List<InterestType> resolvePreferenceTags(List<InterestType> preferenceTags) {
    if (preferenceTags == null || preferenceTags.isEmpty()) {
      return List.of(InterestType.values());
    }
    return preferenceTags;
  }

  private int resolveStayDurationMinutes(Integer stayDurationMinutes) {
    return stayDurationMinutes != null ? stayDurationMinutes : DEFAULT_STAY_DURATION_MINUTES;
  }

  private CompanionType resolveCompanionType(CompanionType companionType) {
    return companionType != null ? companionType : DEFAULT_COMPANION_TYPE;
  }

  private Course toCourse(
      AiRecommendedCourse aiCourse,
      InterestType representativeInterestType,
      int durationMinutes,
      EntranceGate entrance,
      EntranceGate exit
  ) {
    Course course = Course.builder()
        .title(aiCourse.title())
        .interestType(representativeInterestType)
        .tagLabel(aiCourse.reason())
        .durationMinutes(durationMinutes)
        .entrance(entrance)
        .exit(exit)
        .build();

    List<CoursePlace> places = aiCourse.stops().stream()
        .map(stop -> CoursePlace.builder()
            .course(course)
            .visitOrder(stop.order())
            .facilityId(stop.facilityId())
            .build())
        .toList();

    course.setPlaces(places);

    return course;
  }

}
