package com.chuseok22.eodaegoserver.domain.course.service;

import com.chuseok22.eodaegoserver.domain.course.CourseFavoriteSortType;
import com.chuseok22.eodaegoserver.domain.course.dto.response.CourseFavoriteItemResponse;
import com.chuseok22.eodaegoserver.domain.course.dto.response.CourseFavoriteListResponse;
import com.chuseok22.eodaegoserver.domain.course.dto.response.CourseFavoriteResponse;
import com.chuseok22.eodaegoserver.domain.course.dto.response.CoursePlaceCatalogInfo;
import com.chuseok22.eodaegoserver.domain.course.entity.CourseFavorite;
import com.chuseok22.eodaegoserver.domain.course.entity.CoursePlace;
import com.chuseok22.eodaegoserver.domain.course.repository.CourseFavoriteRepository;
import com.chuseok22.eodaegoserver.domain.course.repository.CourseRepository;
import com.chuseok22.eodaegoserver.global.exception.CustomException;
import com.chuseok22.eodaegoserver.global.exception.ErrorCode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CourseFavoriteService {

  private final CourseFavoriteRepository courseFavoriteRepository;
  private final CourseRepository courseRepository;
  private final CoursePlaceCatalogResolver catalogResolver;
  private final Clock clock;

  @Transactional
  public CourseFavoriteResponse addFavorite(UUID memberId, UUID courseId) {

    if (!courseRepository.existsById(courseId)) {
      log.warn("즐겨찾기 등록 실패. 존재하지 않는 코스. courseId={}", courseId);
      throw new CustomException(ErrorCode.COURSE_NOT_FOUND);
    }

    int inserted = courseFavoriteRepository.insertIfAbsent(memberId, courseId, LocalDateTime.now(clock));
    if (inserted == 0) {
      log.debug("이미 즐겨찾기된 코스. memberId={}, courseId={}", memberId, courseId);
    } else {
      log.info("즐겨찾기 등록 완료. memberId={}, courseId={}", memberId, courseId);
    }

    return courseFavoriteRepository.findByMemberIdAndCourseId(memberId, courseId)
        .map(CourseFavoriteResponse::from)
        .orElseThrow(() -> {
          log.warn("즐겨찾기 등록 직후 조회 실패. 동시 삭제 요청과 경합한 것으로 보임. memberId={}, courseId={}",
              memberId, courseId);
          return new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        });
  }

  public CourseFavoriteListResponse getFavorites(UUID memberId, CourseFavoriteSortType sortType) {
    List<CourseFavorite> favorites = courseFavoriteRepository.findByMemberId(memberId, sortType.getSort());

    List<Long> facilityIds = favorites.stream()
        .flatMap(favorite -> favorite.getCourse().getPlaces().stream())
        .map(CoursePlace::getFacilityId)
        .distinct()
        .toList();

    Map<Long, CoursePlaceCatalogInfo> catalogInfoByFacilityId = catalogResolver.resolve(memberId, facilityIds);

    List<CourseFavoriteItemResponse> items = favorites.stream()
        .map(favorite -> CourseFavoriteItemResponse.from(favorite, catalogInfoByFacilityId))
        .toList();

    return CourseFavoriteListResponse.from(items);
  }

  @Transactional
  public void deleteFavorite(UUID memberId, UUID courseId) {

    int deleted = courseFavoriteRepository.deleteByMemberIdAndCourseId(memberId, courseId);
    if (deleted == 0) {
      log.debug("즐겨찾기 삭제 요청. 이미 즐겨찾기되어 있지 않음. memberId={}, courseId={}", memberId, courseId);
      return;
    }

    log.info("즐겨찾기 삭제 완료. memberId={}, courseId={}", memberId, courseId);
  }
}
