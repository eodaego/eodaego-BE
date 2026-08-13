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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.chuseok22.eodaegoserver.domain.catalog.entity.MemberCatalogCollection;
import com.chuseok22.eodaegoserver.domain.catalog.repository.MemberCatalogCollectionRepository;
import java.util.Set;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CourseRecommendationService {

  private static final String GATE_CATEGORY = "출입문";

  private final CourseRepository courseRepository;
  private final CourseFavoriteRepository courseFavoriteRepository;
  private final CatalogItemRepository catalogItemRepository;
  private final CourseAiClient courseAiClient;
  private final MemberCatalogCollectionRepository memberCatalogCollectionRepository;

  @Transactional
  public List<CourseResponse> recommendCourses(CourseRecommendationRequest request, UUID memberId) {

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
        .filter(this::hasVisitablePlace)
        .map(courseRepository::save)
        .toList();

    if (savedCourses.isEmpty()) {
      log.warn("방문 가능한 장소를 가진 코스가 없습니다. AI 추천 코스 수={}", aiResponse.courses().size());
      throw new CustomException(ErrorCode.AI_SERVER_UNAVAILABLE);
    }

    log.info("코스 추천 완료. 추천된 코스 수={}", savedCourses.size());

    Set<UUID> collectedCatalogItemIds =
      findCollectedCatalogItemIds(
        memberId,
        catalogItemsByFacilityId
      );

    return savedCourses.stream()
        .map(course -> CourseResponse.from(
          course,
          false,
          catalogItemsByFacilityId,
          collectedCatalogItemIds
        ))
        .toList();

  }

  public CourseResponse getCourse(UUID courseId, UUID memberId) {
    Course course = courseRepository.findWithPlacesById(courseId)
        .orElseThrow(() -> {
          log.warn("코스 조회 실패. courseId={}", courseId);
          throw new CustomException(ErrorCode.COURSE_NOT_FOUND);
        });

    boolean favorite = courseFavoriteRepository.existsByMemberIdAndCourseId(memberId, courseId);

    Map<Long, CatalogItem> catalogItemsByFacilityId = findCatalogItemsByFacilityId(course);
    Set<UUID> collectedCatalogItemIds = findCollectedCatalogItemIds(memberId, catalogItemsByFacilityId);

    return CourseResponse.from(
      course,
      favorite,
      catalogItemsByFacilityId,
      collectedCatalogItemIds
    );
  }

  private Map<Long, CatalogItem> findCatalogItemsByFacilityId(AiRouteRecommendationResponse response) {
    List<Long> facilityIds = response.courses().stream()
        .flatMap(course -> course.stops().stream())
        .map(AiRouteStop::facilityId)
        .distinct()
        .toList();

    return catalogItemRepository.findByCategoryAndFacility_AiFacilityIdIn(CatalogCategory.PLACE, facilityIds).stream()
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
        .tagLabels(aiCourse.tagLabels())
        .interestTypes(interestTypes)
        .estimatedDurationMinutes(aiCourse.estimatedDurationMinutes())
        .entrance(entrance)
        .exit(exit)
        .build();

    List<AiRouteStop> visitStops = aiCourse.stops().stream()
        .filter(stop -> !isGate(stop))
        .sorted(Comparator.comparingInt(AiRouteStop::order))
        .toList();

    List<CoursePlace> places = new ArrayList<>();
    for (AiRouteStop stop : visitStops) {
      CatalogItem catalogItem = catalogItemsByFacilityId.get(stop.facilityId());

      String name = resolveName(catalogItem, stop);
      Double latitude = resolveLatitude(catalogItem, stop);
      Double longitude = resolveLongitude(catalogItem, stop);

      if (name == null || latitude == null || longitude == null) {
        log.warn("표시에 필요한 정보가 없어 장소를 제외합니다. facilityId={}", stop.facilityId());
        continue;
      }

      places.add(CoursePlace.builder()
          .course(course)
          .visitOrder(places.size() + 1)
          .facilityId(stop.facilityId())
          .name(name)
          .category(toCatalogCategory(stop.facilityCategory()))
          .latitude(latitude)
          .longitude(longitude)
          .build());
    }

    course.setPlaces(places);

    return course;
  }

  private String resolveName(CatalogItem catalogItem, AiRouteStop stop) {
    if (catalogItem != null && catalogItem.getName() != null) {
      return catalogItem.getName();
    }
    return stop.facility().name();
  }

  private Double resolveLatitude(CatalogItem catalogItem, AiRouteStop stop) {
    if (catalogItem != null && catalogItem.getLatitude() != null) {
      return catalogItem.getLatitude();
    }
    return stop.facility().latitude();
  }

  private Double resolveLongitude(CatalogItem catalogItem, AiRouteStop stop) {
    if (catalogItem != null && catalogItem.getLongitude() != null) {
      return catalogItem.getLongitude();
    }
    return stop.facility().longitude();
  }

  private boolean hasVisitablePlace(Course course) {
    if (course.getPlaces().isEmpty()) {
      log.warn("방문 가능한 장소가 없어 코스를 제외합니다. title={}", course.getTitle());
      return false;
    }
    return true;
  }

  private boolean isGate(AiRouteStop stop) {
    String category = stop.facilityCategory();
    return category != null && GATE_CATEGORY.equals(category.trim());
  }

  private CatalogCategory toCatalogCategory(String facilityCategory) {
    if (facilityCategory == null || facilityCategory.isBlank()) {
      log.debug("AI 시설 category가 비어 있어 PLACE로 처리합니다.");
      return CatalogCategory.PLACE;
    }

    String normalizedCategory = facilityCategory.trim();

    return switch (normalizedCategory) {
      case "동물나라", "ANIMAL" -> CatalogCategory.ANIMAL;
      case "자연나라", "PLANT", "조경시설", "체험시설" -> CatalogCategory.PLANT;
      case "PLACE" -> CatalogCategory.PLACE;
      default -> {
        log.debug("정의되지 않은 AI 시설 category를 PLACE로 처리합니다. category={}", normalizedCategory);
        yield CatalogCategory.PLACE;
      }
    };
  }

  private Set<UUID> findCollectedCatalogItemIds(UUID memberId, Map<Long, CatalogItem> catalogItemsByFacilityId) {
    List<UUID> catalogItemIds = catalogItemsByFacilityId.values().stream()
      .map(CatalogItem::getId)
      .distinct()
      .toList();

    if (catalogItemIds.isEmpty()) {
      return Set.of();
    }

    return memberCatalogCollectionRepository
      .findByMemberIdAndCatalogItemIdIn(memberId, catalogItemIds)
      .stream()
      .map(collection -> collection.getCatalogItem().getId())
      .collect(Collectors.toSet());
  }

  private Map<Long, CatalogItem> findCatalogItemsByFacilityId(Course course) {
    List<Long> facilityIds = course.getPlaces().stream()
      .map(CoursePlace::getFacilityId)
      .distinct()
      .toList();

    return catalogItemRepository
      .findByCategoryAndSource_ExternalIdIn(
        CatalogCategory.PLACE,
        facilityIds
      )
      .stream()
      .collect(Collectors.toMap(
        CatalogItem::getExternalId,
        Function.identity()
      ));
  }

}
