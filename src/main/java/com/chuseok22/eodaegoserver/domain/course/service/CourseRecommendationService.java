package com.chuseok22.eodaegoserver.domain.course.service;

import com.chuseok22.eodaegoserver.domain.catalog.CatalogCategory;
import com.chuseok22.eodaegoserver.domain.catalog.entity.CatalogItem;
import com.chuseok22.eodaegoserver.domain.catalog.repository.CatalogItemRepository;
import com.chuseok22.eodaegoserver.domain.course.CompanionType;
import com.chuseok22.eodaegoserver.domain.course.EntranceGate;
import com.chuseok22.eodaegoserver.domain.course.InterestType;
import com.chuseok22.eodaegoserver.domain.course.dto.external.AiRecommendedCourse;
import com.chuseok22.eodaegoserver.domain.course.dto.external.AiRouteRecommendationRequest;
import com.chuseok22.eodaegoserver.domain.course.dto.external.AiRouteRecommendationResponse;
import com.chuseok22.eodaegoserver.domain.course.dto.external.AiRouteStop;
import com.chuseok22.eodaegoserver.domain.course.dto.request.CourseRecommendationRequest;
import com.chuseok22.eodaegoserver.domain.course.dto.response.CourseResponse;
import com.chuseok22.eodaegoserver.domain.course.entity.Course;
import com.chuseok22.eodaegoserver.domain.course.entity.CoursePlace;
import com.chuseok22.eodaegoserver.domain.course.repository.CourseFavoriteRepository;
import com.chuseok22.eodaegoserver.domain.course.repository.CourseRepository;
import com.chuseok22.eodaegoserver.global.exception.CustomException;
import com.chuseok22.eodaegoserver.global.exception.ErrorCode;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CourseRecommendationService {

  private final CourseRepository courseRepository;
  private final CourseFavoriteRepository courseFavoriteRepository;
  private final CatalogItemRepository catalogItemRepository;
  private final CourseAiClient courseAiClient;

  @Transactional
  public List<CourseResponse> recommendCourses(CourseRecommendationRequest request) {

    AiRouteRecommendationRequest aiRequest = new AiRouteRecommendationRequest(
        request.interestTypes(),
        request.stayDurationMinutes(),
        request.entrance(),
        request.exit(),
        request.companionType()
    );

    AiRouteRecommendationResponse aiResponse = courseAiClient.recommendRoutes(aiRequest);

    Map<Long, CatalogItem> catalogItemsByFacilityId = findCatalogItemsByFacilityId(aiResponse);

    List<Course> savedCourses = aiResponse.courses().stream()
        .map(aiCourse -> toCourse(aiCourse, request.interestTypes(), request.entrance(), request.exit(), catalogItemsByFacilityId))
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

  private Map<Long, CatalogItem> findCatalogItemsByFacilityId(AiRouteRecommendationResponse response) {
    List<Long> facilityIds = response.courses().stream()
        .flatMap(course -> course.stops().stream())
        .map(AiRouteStop::facilityId)
        .distinct()
        .toList();

    return catalogItemRepository.findByCategoryAndExternalIdIn(CatalogCategory.PLACE, facilityIds).stream()
        .collect(Collectors.toMap(CatalogItem::getExternalId, Function.identity()));
  }

  private Course toCourse(
      AiRecommendedCourse aiCourse,
      List<InterestType> interestTypes,
      EntranceGate entrance,
      EntranceGate exit,
      Map<Long, CatalogItem> catalogItemsByFacilityId
  ) {
    Course course = Course.builder()
        .title(aiCourse.title())
        .interestTypes(interestTypes)
        .durationMinutes(aiCourse.durationMinutes())
        .entrance(entrance)
        .exit(exit)
        .build();

    List<CoursePlace> places = aiCourse.stops().stream()
        .map(stop -> {
          CatalogItem catalogItem = catalogItemsByFacilityId.get(stop.facilityId());
          return CoursePlace.builder()
              .course(course)
              .visitOrder(stop.order())
              .facilityId(stop.facilityId())
              .name(catalogItem != null ? catalogItem.getName() : null)
              .category(catalogItem != null ? catalogItem.getCategory() : null)
              .latitude(catalogItem != null ? catalogItem.getLatitude() : null)
              .longitude(catalogItem != null ? catalogItem.getLongitude() : null)
              .build();
        })
        .toList();

    course.setPlaces(places);

    return course;
  }
}
