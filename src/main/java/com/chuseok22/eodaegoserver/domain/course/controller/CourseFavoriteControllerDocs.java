package com.chuseok22.eodaegoserver.domain.course.controller;

import com.chuseok22.apichangelog.annotation.ApiChangeLog;
import com.chuseok22.apichangelog.annotation.ApiChangeLogs;
import com.chuseok22.eodaegoserver.domain.course.CourseFavoriteSortType;
import com.chuseok22.eodaegoserver.domain.course.dto.request.CourseFavoriteRequest;
import com.chuseok22.eodaegoserver.domain.course.dto.response.CourseFavoriteListResponse;
import com.chuseok22.eodaegoserver.domain.course.dto.response.CourseFavoriteResponse;
import com.chuseok22.eodaegoserver.global.exception.ErrorResponse;
import com.chuseok22.eodaegoserver.global.swagger.ChangeLogAuthor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;


@Tag(name = "Course Favorite", description = "코스 즐겨찾기 API")
public interface CourseFavoriteControllerDocs {

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2026-07-16",
          author = ChangeLogAuthor.KIM_JAEHYEON,
          description = "코스 즐겨찾기 API 최초 작성",
          issueUrl = "https://github.com/eodaego/eodaego-BE/issues/29"
      ),
      @ApiChangeLog(
        date = "2026-07-27",
        author = ChangeLogAuthor.KANG_JIYUN,
        description = "코스 즐겨찾기 등록 요청을 경로 변수에서 요청 본문 방식으로 변경",
        issueUrl = "https://github.com/eodaego/eodaego-BE/issues/43"
      )
  })
  @Operation(
      summary = "코스 즐겨찾기 등록",
      description = """
          요청 본문의 courseId에 해당하는 코스를 즐겨찾기에 등록한다.

          - 몇 번을 호출해도 결과가 같아야 하는 액션이라(좋아요/북마크류와 동일한 성격),
            이미 즐겨찾기된 코스를 다시 등록 요청해도 에러 없이 최초 등록 시점의 정보를 그대로 반환한다.
          - Authorization: Bearer {accessToken} 헤더가 반드시 필요하다.
          """,
      security = @SecurityRequirement(name = "Bearer Token")
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "등록 성공(이미 등록되어 있었다면 기존 즐겨찾기 정보 반환)"),
      @ApiResponse(responseCode = "401", description = "Authorization 헤더가 없거나 accessToken이 유효하지 않음. errorCode: UNAUTHORIZED",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "404", description = """
          존재하지 않는 코스 또는 회원.
          - COURSE_NOT_FOUND: 존재하지 않는 코스
          - MEMBER_NOT_FOUND: 존재하지 않는 회원
          """,
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<CourseFavoriteResponse> addFavorite(UUID memberId, CourseFavoriteRequest request);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2026-07-16",
          author = ChangeLogAuthor.KIM_JAEHYEON,
          description = "코스 즐겨찾기 API 최초 작성",
          issueUrl = "https://github.com/eodaego/eodaego-BE/issues/29"
      )
  })
  @Operation(
      summary = "코스 즐겨찾기 삭제",
      description = """
          courseId에 해당하는 즐겨찾기를 삭제한다.

          - Authorization: Bearer {accessToken} 헤더가 반드시 필요하다.
          """,
      security = @SecurityRequirement(name = "Bearer Token")
  )
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "삭제 성공"),
      @ApiResponse(responseCode = "401", description = "Authorization 헤더가 없거나 accessToken이 유효하지 않음. errorCode: UNAUTHORIZED",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "404", description = "즐겨찾기되어 있지 않은 코스. errorCode: COURSE_NOT_FOUND",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<Void> deleteFavorite(UUID memberId, UUID courseId);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2026-07-16",
          author = ChangeLogAuthor.KIM_JAEHYEON,
          description = "코스 즐겨찾기 API 최초 작성",
          issueUrl = "https://github.com/eodaego/eodaego-BE/issues/29"
      ),
      @ApiChangeLog(
        date = "2026-07-27",
        author = ChangeLogAuthor.KANG_JIYUN,
        description = "즐겨찾기 목록 정렬 조건과 전체 개수를 포함한 응답 형식 추가",
        issueUrl = "https://github.com/eodaego/eodaego-BE/issues/43"
      )
  })
  @Operation(
      summary = "즐겨찾기한 코스 목록 조회",
      description = """
          현재 인증된 회원이 즐겨찾기한 코스 목록을 반환한다.
        
          정렬 기준:
          - sort=SAVED_AT: 즐겨찾기에 저장한 시각 기준
          - sort=DURATION: 코스 소요시간 기준
          - order=ASC: 오름차순
          - order=DESC: 내림차순
          - 기본값은 sort=SAVED_AT, order=DESC이다.
        
          - Authorization: Bearer {accessToken} 헤더가 반드시 필요하다.
          """,
      security = @SecurityRequirement(name = "Bearer Token")
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공(즐겨찾기한 코스가 없으면 빈 배열)"),
      @ApiResponse(responseCode = "401", description = "Authorization 헤더가 없거나 accessToken이 유효하지 않음. errorCode: UNAUTHORIZED",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<CourseFavoriteListResponse> getFavorites(UUID memberId, CourseFavoriteSortType sort, Sort.Direction order);
}
