package com.chuseok22.eodaegoserver.domain.course.service;

import com.chuseok22.eodaegoserver.domain.course.CourseFavoriteSortType;
import com.chuseok22.eodaegoserver.domain.course.dto.response.CourseFavoriteItemResponse;
import com.chuseok22.eodaegoserver.domain.course.dto.response.CourseFavoriteListResponse;
import com.chuseok22.eodaegoserver.domain.course.dto.response.CourseFavoriteResponse;
import com.chuseok22.eodaegoserver.domain.course.dto.response.CoursePlaceCatalogInfo;
import com.chuseok22.eodaegoserver.domain.course.entity.Course;
import com.chuseok22.eodaegoserver.domain.course.entity.CourseFavorite;
import com.chuseok22.eodaegoserver.domain.course.entity.CoursePlace;
import com.chuseok22.eodaegoserver.domain.course.repository.CourseFavoriteRepository;
import com.chuseok22.eodaegoserver.domain.course.repository.CourseRepository;
import com.chuseok22.eodaegoserver.domain.member.entity.Member;
import com.chuseok22.eodaegoserver.domain.member.repository.MemberRepository;
import com.chuseok22.eodaegoserver.global.exception.CustomException;
import com.chuseok22.eodaegoserver.global.exception.ErrorCode;
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
  private final MemberRepository memberRepository;
  private final CoursePlaceCatalogResolver catalogResolver;

  @Transactional
  public CourseFavoriteResponse addFavorite(UUID memberId, UUID courseId) {

    return courseFavoriteRepository.findByMemberIdAndCourseId(memberId, courseId)
        .map(existing -> {
          log.debug("이미 즐겨찾기된 코스. memberId={}, courseId={}", memberId, courseId);
          return CourseFavoriteResponse.from(existing);
        })
        .orElseGet(() -> {

          Course course = courseRepository.findById(courseId)
              .orElseThrow(() -> {
                log.warn("즐겨찾기 등록 실패. 존재하지 않는 코스. courseId={}", courseId);
                throw new CustomException(ErrorCode.COURSE_NOT_FOUND);
              });

          Member member = memberRepository.findById(memberId)
              .orElseThrow(() -> {
                log.warn("즐겨찾기 등록 실패. 존재하지 않는 회원. memberId={}", memberId);
                throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
              });

          CourseFavorite courseFavorite = courseFavoriteRepository.save(
              CourseFavorite.builder()
                  .member(member)
                  .course(course)
                  .build()
          );

          log.info("즐겨찾기 등록 완료. memberId={}, courseId={}", memberId, courseId);

          return CourseFavoriteResponse.from(courseFavorite);
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

    courseFavoriteRepository.findByMemberIdAndCourseId(memberId, courseId)
        .ifPresentOrElse(
            courseFavorite -> {
              courseFavoriteRepository.delete(courseFavorite);
              log.info("즐겨찾기 삭제 완료. memberId={}, courseId={}", memberId, courseId);
            },
            () -> log.debug("즐겨찾기 삭제 요청. 이미 즐겨찾기되어 있지 않음. memberId={}, courseId={}", memberId, courseId)
        );
  }


}
