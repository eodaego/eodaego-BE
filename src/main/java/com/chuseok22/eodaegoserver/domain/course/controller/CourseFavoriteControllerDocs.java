package com.chuseok22.eodaegoserver.domain.course.controller;

import com.chuseok22.apichangelog.annotation.ApiChangeLog;
import com.chuseok22.apichangelog.annotation.ApiChangeLogs;
import com.chuseok22.eodaegoserver.domain.course.CourseFavoriteSortType;
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
import org.springframework.http.ResponseEntity;

@Tag(name = "Course Favorite", description = "코스 즐겨찾기 API")
public interface CourseFavoriteControllerDocs {

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2026-07-16",
          author = ChangeLogAuthor.KIM_JAEHYEON,
          description = "코스 즐겨찾기 API 최초 작성",
          issueUrl = "https://github.com/eodaego/eodaego-BE/issues/29"
      )
  })
  @Operation(
      summary = "코스 즐겨찾기 등록",
      description = """
          courseId를 즐겨찾기에 등록한다.

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
  ResponseEntity<CourseFavoriteResponse> addFavorite(UUID memberId, UUID courseId);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2026-07-16",
          author = ChangeLogAuthor.KIM_JAEHYEON,
          description = "코스 즐겨찾기 API 최초 작성",
          issueUrl = "https://github.com/eodaego/eodaego-BE/issues/29"
      ),
      @ApiChangeLog(
          date = "2026-07-28",
          author = ChangeLogAuthor.KIM_JAEHYEON,
          description = "삭제를 멱등(idempotent) 처리로 변경. 즐겨찾기되어 있지 않은 코스를 삭제 요청해도 404 대신 204로 성공 응답",
          issueUrl = "https://github.com/eodaego/eodaego-BE/issues/29"
      )
  })
  @Operation(
      summary = "코스 즐겨찾기 삭제",
      description = """
          courseId에 해당하는 즐겨찾기를 삭제한다.

          - 멱등(idempotent) 동작이다. 이미 즐겨찾기되어 있지 않은(또는 존재하지 않는) 코스를 삭제 요청해도
            에러 없이 204로 응답한다. 클라이언트 의도가 "이 코스를 즐겨찾기에서 제외"이므로 이미 제외된 상태도 성공으로 본다.
            등록(addFavorite)이 멱등인 것과 대칭이며, 더블클릭/재시도 시에도 안전하다.
          - Authorization: Bearer {accessToken} 헤더가 반드시 필요하다.
          """,
      security = @SecurityRequirement(name = "Bearer Token")
  )
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "삭제 성공(이미 즐겨찾기되어 있지 않았더라도 204)"),
      @ApiResponse(responseCode = "401", description = "Authorization 헤더가 없거나 accessToken이 유효하지 않음. errorCode: UNAUTHORIZED",
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
      ),
      @ApiChangeLog(
          date = "2026-08-03",
          author = ChangeLogAuthor.KIM_JAEHYEON,
          description = "정렬 파라미터를 sort/order 두 개에서 sort 하나로 통합(LATEST/OLDEST/DURATION_SHORT/DURATION_LONG). 허용 목록 밖의 sort 값에 400 응답 추가",
          issueUrl = "https://github.com/eodaego/eodaego-BE/issues/43"
      )
  })
  @Operation(
      summary = "즐겨찾기한 코스 목록 조회",
      description = """
          현재 인증된 회원이 즐겨찾기한 코스 목록을 sort 기준으로 정렬해 반환한다.
          각 항목은 즐겨찾기한 시각과 코스 상세 정보를 함께 담고 있다.

          **정렬 기준 (query parameter `sort`)**

          정렬 필드와 방향이 하나의 값에 묶여 있다. 아래 4가지만 허용되며, 생략하면 `LATEST`가 적용된다.

          - `LATEST`: 최근에 즐겨찾기한 코스부터 (즐겨찾기 시각 내림차순) — **기본값**
          - `OLDEST`: 오래전에 즐겨찾기한 코스부터 (즐겨찾기 시각 오름차순)
          - `DURATION_SHORT`: 코스 예상 소요시간이 짧은 것부터 (오름차순)
          - `DURATION_LONG`: 코스 예상 소요시간이 긴 것부터 (내림차순)

          `DURATION_SHORT`/`DURATION_LONG`은 소요시간이 같은 코스끼리 순서가 요청마다 뒤바뀌지 않도록
          즐겨찾기 시각 내림차순을 2차 정렬 기준으로 함께 적용한다.

          **응답 형태**

          `totalCount`(즐겨찾기 총 개수)와 `items`(정렬된 목록)를 담은 객체를 반환한다.
          즐겨찾기한 코스가 없으면 `totalCount`는 0, `items`는 빈 배열이다.
          페이지네이션은 아직 적용하지 않으므로 `totalCount`는 항상 `items`의 길이와 같다.

          - Authorization: Bearer {accessToken} 헤더가 반드시 필요하다.
          """,
      security = @SecurityRequirement(name = "Bearer Token")
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공(즐겨찾기한 코스가 없으면 totalCount 0, items 빈 배열)"),
      @ApiResponse(responseCode = "400", description = """
          sort에 허용 목록 밖의 값을 보냄. errorCode: INVALID_REQUEST
          - fieldErrors[0].field에 파라미터명 "sort", fieldErrors[0].reason에 "허용되지 않는 값입니다: {보낸값}"이 담긴다.
          """,
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "401", description = "Authorization 헤더가 없거나 accessToken이 유효하지 않음. errorCode: UNAUTHORIZED",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<CourseFavoriteListResponse> getFavorites(UUID memberId, CourseFavoriteSortType sort);
}
