package com.chuseok22.eodaegoserver.domain.facility.controller;

import com.chuseok22.apichangelog.annotation.ApiChangeLog;
import com.chuseok22.apichangelog.annotation.ApiChangeLogs;
import com.chuseok22.eodaegoserver.domain.facility.dto.response.FacilityDetailResponse;
import com.chuseok22.eodaegoserver.domain.facility.dto.response.FacilitySummaryResponse;
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
import org.springframework.http.ResponseEntity;

@Tag(name = "Facility", description = "회원용 시설(장소) 조회 API")
public interface FacilityControllerDocs {

  @ApiChangeLogs({
    @ApiChangeLog(
      date = "2026-08-08",
      author = ChangeLogAuthor.KANG_JIYUN,
      description = "회원용 시설 전체 조회 API 추가",
      issueUrl = "https://github.com/eodaego/eodaego-BE/issues/65"
    ),
    @ApiChangeLog(
      date = "2026-08-11",
      author = ChangeLogAuthor.KIM_JAEHYEON,
      description = "조회 방식을 AI 서버 실시간 프록시에서 BE DB 조회로 변경. AI 서버 장애 시에도 200을 반환하므로 503 응답이 사라졌다. 응답에서 code 필드가 제거됐다(출입문 전용 값이라 앱에서 쓰이지 않음)",
      issueUrl = "https://github.com/eodaego/eodaego-BE/issues/65"
    )
  })
  @Operation(
    summary = "시설 전체 조회",
    description = """
        지도에 표시할 공원 시설 전체를 반환한다.

        **도감과 다른 목록이다.** 회원이 사진으로 수집하는 도감(`GET /catalog`)과 달리,
        여기에는 수집 대상이 아닌 **출입문 11곳과 고객안내센터도 포함**된다.
        지도에는 정문·후문이 찍혀야 하기 때문이다. 현재 총 37건이다.

        **id 해석**
        `id`는 AI 서버가 매긴 시설 번호이며, 아래 두 값과 **정확히 같은 값**이다.
        - 코스 추천 응답(`POST /courses/recommendations`)의 `places[].facilityId`
        - 도감 응답의 `externalId`

        따라서 지도 마커를 눌렀을 때 그 장소의 도감 수집 여부를 확인하려면
        이름으로 검색하지 말고 이 `id`로 매칭하면 된다.

        **좌표가 없는 시설이 있다**
        `latitude`/`longitude`는 null일 수 있다. 지도 마커를 그리기 전에 반드시 확인해야 한다.

        **정렬**
        `id` 오름차순으로 고정 반환한다. 별도 정렬 파라미터는 없다.

        **응답 출처**
        BE DB에서 조회하며 요청 시점에 AI 서버를 호출하지 않는다.
        AI 서버가 중단돼도 이 API는 정상 동작한다(데이터가 갱신되지 않을 뿐이다).

        Authorization: Bearer {accessToken} 헤더가 필요하다.
        """,
    security = @SecurityRequirement(name = "Bearer Token")
  )
  @ApiResponses({
    @ApiResponse(
      responseCode = "200",
      description = "조회 성공. 시설이 하나도 없으면 빈 배열을 반환한다"
    ),
    @ApiResponse(
      responseCode = "401",
      description = "Authorization 헤더가 없거나 accessToken이 유효하지 않음. errorCode: UNAUTHORIZED",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
  })
  ResponseEntity<List<FacilitySummaryResponse>> getFacilities();

  @ApiChangeLogs({
    @ApiChangeLog(
      date = "2026-08-08",
      author = ChangeLogAuthor.KANG_JIYUN,
      description = "회원용 시설 상세 조회 API 추가",
      issueUrl = "https://github.com/eodaego/eodaego-BE/issues/65"
    ),
    @ApiChangeLog(
      date = "2026-08-11",
      author = ChangeLogAuthor.KIM_JAEHYEON,
      description = "조회 방식을 AI 서버 실시간 프록시에서 BE DB 조회로 변경(503 응답 삭제). 운영시간 응답이 공원 공통 HTML 목록(operatingHours)에서 시설별 openTime/closeTime/operatingNote로 대체됐다. 응답에서 code 필드가 제거됐다",
      issueUrl = "https://github.com/eodaego/eodaego-BE/issues/65"
    )
  })
  @Operation(
    summary = "시설 상세 조회",
    description = """
        시설 ID로 상세 정보를 조회한다. `id`는 전체 조회 응답의 `id`와 동일한 값이며,
        도감 항목 ID(UUID)가 아니다.

        **출입문·고객안내센터도 조회된다.** 도감에 없는 시설이지만 지도에서 마커를 누를 수 있으므로
        상세 정보를 반환한다. 다만 이런 시설은 `intro`/`description`/`facilityType`이 대체로 null이다.

        **운영시간 3필드**
        `openTime`/`closeTime`/`operatingNote`는 AI 서버가 제공하지 않는 값으로 BE가 직접 관리한다.
        - 두 시각이 모두 있으면 현재 시각과 비교해 "운영중 / 운영종료"를 판단할 수 있다.
        - **아직 채워지지 않은 시설이 많아 null인 경우가 일반적이다.** null이면 운영시간 영역을
          그리지 않는 편이 낫다.
        - 하절기·동절기 차이나 휴관일처럼 시각으로 표현할 수 없는 내용은 `operatingNote`에 문장으로 담긴다.
          `openTime`/`closeTime`이 null이어도 `operatingNote`만 있을 수 있다.

        **응답 출처**
        BE DB에서 조회하며 요청 시점에 AI 서버를 호출하지 않는다.

        Authorization: Bearer {accessToken} 헤더가 필요하다.
        """,
    security = @SecurityRequirement(name = "Bearer Token")
  )
  @ApiResponses({
    @ApiResponse(
      responseCode = "200",
      description = "조회 성공"
    ),
    @ApiResponse(
      responseCode = "401",
      description = "Authorization 헤더가 없거나 accessToken이 유효하지 않음. errorCode: UNAUTHORIZED",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    ),
    @ApiResponse(
      responseCode = "404",
      description = "해당 id의 시설이 없음. errorCode: FACILITY_NOT_FOUND",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
  })
  ResponseEntity<FacilityDetailResponse> getFacility(Long facilityId);
}
