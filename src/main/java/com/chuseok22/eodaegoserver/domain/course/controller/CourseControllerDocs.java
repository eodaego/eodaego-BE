package com.chuseok22.eodaegoserver.domain.course.controller;

import com.chuseok22.apichangelog.annotation.ApiChangeLog;
import com.chuseok22.apichangelog.annotation.ApiChangeLogs;
import com.chuseok22.eodaegoserver.domain.course.dto.request.CourseRecommendationRequest;
import com.chuseok22.eodaegoserver.domain.course.dto.response.CourseResponse;
import com.chuseok22.eodaegoserver.global.exception.ErrorResponse;
import com.chuseok22.eodaegoserver.global.swagger.ChangeLogAuthor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;

@Tag(name = "Course", description = "코스 추천/조회 API")
public interface CourseControllerDocs {

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2026-07-16",
          author = ChangeLogAuthor.KIM_JAEHYEON,
          description = "코스 추천 API 최초 작성",
          issueUrl = "https://github.com/eodaego/eodaego-BE/issues/29"
      )
  })
  @Operation(
      summary = "코스 추천 요청",
      description = """
          관심 태그/체류 시간/입출구/동행 유형을 기준으로 외부 AI 서버에 코스 추천을 요청하고,
          추천된 코스를 그대로 저장해 반환한다.

          - entrance/exit는 필수이며 건너뛸 수 없다.
          - interestTypes/stayDurationMinutes/companionType은 건너뛰면(null) AI 서버에도 null 그대로 전달된다(BE가 별도 기본값을 채우지 않는다).
          - 응답 코스의 durationMinutes는 AI 서버가 실제로 계산해 응답한 값을 그대로 저장한 것이다(요청의 stayDurationMinutes를 그대로 저장하는 것이 아니다).
          - 응답 코스 수는 AI 서버 응답에 따라 달라지며 고정된 개수를 보장하지 않는다.
          - 각 코스의 장소(places)는 AI가 준 facilityId를 도감(catalog_item, category=PLACE)의 externalId와 매칭해 이름/좌표를 채운다. 아직 도감에 동기화되지 않은 시설이면 해당 장소의 name/category/latitude/longitude는 null이다.
          - 방금 생성된 코스이므로 응답의 favorite는 항상 false다.
          - Authorization: Bearer {accessToken} 헤더가 반드시 필요하다.
          """,
      security = @SecurityRequirement(name = "Bearer Token")
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "추천 및 저장 성공"),
      @ApiResponse(responseCode = "400", description = "요청 값 검증 실패(entrance/exit 누락, stayDurationMinutes가 양수 아님 등). errorCode: INVALID_REQUEST",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "401", description = "Authorization 헤더가 없거나 accessToken이 유효하지 않음. errorCode: UNAUTHORIZED",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "503", description = "외부 AI 서버 호출 실패. errorCode: AI_SERVER_UNAVAILABLE",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<List<CourseResponse>> recommendCourses(CourseRecommendationRequest request);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2026-07-16",
          author = ChangeLogAuthor.KIM_JAEHYEON,
          description = "코스 추천 API 최초 작성",
          issueUrl = "https://github.com/eodaego/eodaego-BE/issues/29"
      )
  })
  @Operation(
      summary = "코스 상세 조회",
      description = """
          courseId로 코스 상세 정보를 조회한다. 응답의 favorite는 현재 인증된 회원이
          이 코스를 즐겨찾기했는지 여부를 나타낸다.

          - Authorization: Bearer {accessToken} 헤더가 반드시 필요하다.
          """,
      security = @SecurityRequirement(name = "Bearer Token")
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "401", description = "Authorization 헤더가 없거나 accessToken이 유효하지 않음. errorCode: UNAUTHORIZED",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 코스. errorCode: COURSE_NOT_FOUND",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<CourseResponse> getCourse(UUID memberId, UUID courseId);
}
