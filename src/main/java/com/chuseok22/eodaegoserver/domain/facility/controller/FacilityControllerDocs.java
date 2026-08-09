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

@Tag(name = "Facility", description = "회원용 시설 조회 API")
public interface FacilityControllerDocs {

  @ApiChangeLogs({
    @ApiChangeLog(
      date = "2026-08-08",
      author = ChangeLogAuthor.KANG_JIYUN,
      description = "회원용 시설 전체 조회 API 추가",
      issueUrl = "https://github.com/eodaego/eodaego-BE/issues/65"
    )
  })
  @Operation(
    summary = "시설 전체 조회",
    description = """
          지도에 표시할 전체 시설을 조회한다.
          출입문과 안내시설을 포함한 모든 시설을 반환한다.
          Authorization: Bearer {accessToken} 헤더가 필요하다.
          """,
    security = @SecurityRequirement(name = "Bearer Token")
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "조회 성공"),
    @ApiResponse(
      responseCode = "401",
      description = "인증 실패",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    ),
    @ApiResponse(
      responseCode = "503",
      description = "AI 서버 연결 실패",
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
    )
  })
  @Operation(
    summary = "시설 상세 조회",
    description = """
          시설 ID를 이용해 시설의 상세 정보를 조회한다.
          소개, 상세 설명, 위치 및 운영시간 정보를 반환한다.
          Authorization: Bearer {accessToken} 헤더가 필요하다.
          """,
    security = @SecurityRequirement(name = "Bearer Token")
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "조회 성공"),
    @ApiResponse(responseCode = "401", description = "인증 실패",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "존재하지 않는 시설. errorCode: FACILITY_NOT_FOUND",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "503", description = "AI 서버 연결 실패",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<FacilityDetailResponse> getFacility(Long facilityId);
}