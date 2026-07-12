package com.chuseok22.eodaegoserver.domain.catalog.controller;

import com.chuseok22.apichangelog.annotation.ApiChangeLog;
import com.chuseok22.apichangelog.annotation.ApiChangeLogs;
import com.chuseok22.eodaegoserver.domain.catalog.CatalogCategory;
import com.chuseok22.eodaegoserver.domain.catalog.dto.response.CatalogItemDetailResponse;
import com.chuseok22.eodaegoserver.domain.catalog.dto.response.CatalogItemListResponse;
import com.chuseok22.eodaegoserver.domain.catalog.dto.response.CatalogSummaryResponse;
import com.chuseok22.eodaegoserver.global.exception.ErrorResponse;
import com.chuseok22.eodaegoserver.global.swagger.ChangeLogAuthor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.ResponseEntity;

@Tag(name = "Catalog", description = "도감 조회 API")
public interface CatalogControllerDocs {

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2026-07-12",
          author = ChangeLogAuthor.KIM_JAEHYEON,
          description = "도감 목록 조회 API 최초 작성",
          issueUrl = "https://github.com/eodaego/eodaego-BE/issues/12"
      )
  })
  @Operation(
      summary = "도감 목록 조회",
      description = """
          카테고리/이름으로 필터링된 도감 항목 목록과, 현재 회원의 수집 개수/전체 개수를 함께 반환한다.

          - category를 생략하면 전체 카테고리를 대상으로 조회한다.
          - name은 부분 일치(대소문자 구분)로 검색된다.
          - 미수집 항목은 name/imageUrl이 null로 내려간다(프론트에서 '?'로 표시).
          - collectedCount는 현재 필터(category) 기준 회원의 수집 개수, items의 개수가 곧 현재 필터 기준 전체 개수다.
          - Authorization: Bearer {accessToken} 헤더가 반드시 필요하다.
          """,
      security = @SecurityRequirement(name = "Bearer Token")
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "401", description = "Authorization 헤더가 없거나 accessToken이 유효하지 않음. errorCode: UNAUTHORIZED",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<CatalogItemListResponse> getCatalogItems(
      UUID memberId,
      @Parameter(description = "카테고리 필터. ANIMAL(동물), PLANT(식물), PLACE(장소) 중 하나. 생략 시 전체 조회.")
      CatalogCategory category,
      @Parameter(description = "이름 부분 검색어. 생략 시 이름으로 필터링하지 않음.")
      String name
  );

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2026-07-12",
          author = ChangeLogAuthor.KIM_JAEHYEON,
          description = "도감 상세 조회 API 최초 작성",
          issueUrl = "https://github.com/eodaego/eodaego-BE/issues/12"
      )
  })
  @Operation(
      summary = "도감 상세 조회",
      description = """
          도감 항목 하나의 전체 정보(특징, 어린이용 설명, 수집일 등)를 조회한다.

          - 회원이 아직 수집하지 않은 항목은 상세 정보 자체에 접근할 수 없다(필드 마스킹이 아니라 403으로 완전 차단).
          - Authorization: Bearer {accessToken} 헤더가 반드시 필요하다.
          """,
      security = @SecurityRequirement(name = "Bearer Token")
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "401", description = "Authorization 헤더가 없거나 accessToken이 유효하지 않음. errorCode: UNAUTHORIZED",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "403", description = "아직 수집하지 않은 도감 항목. errorCode: CATALOG_ITEM_NOT_COLLECTED",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 도감 항목. errorCode: CATALOG_ITEM_NOT_FOUND",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<CatalogItemDetailResponse> getCatalogItemDetail(UUID memberId, UUID catalogItemId);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2026-07-13",
          author = ChangeLogAuthor.KIM_JAEHYEON,
          description = "도감 항목 수집 API 최초 작성",
          issueUrl = "https://github.com/eodaego/eodaego-BE/issues/12"
      )
  })
  @Operation(
      summary = "도감 항목 수집",
      description = """
          회원이 도감 항목을 수집 처리한다(사진 촬영/퀴즈 등 실제 수집 판정 로직은 아직 없고, catalogItemId만으로 즉시 수집 처리한다).

          - 이미 수집한 항목을 다시 수집 요청하면 409로 거부된다.
          - Authorization: Bearer {accessToken} 헤더가 반드시 필요하다.
          """,
      security = @SecurityRequirement(name = "Bearer Token")
  )
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "수집 성공"),
      @ApiResponse(responseCode = "401", description = "Authorization 헤더가 없거나 accessToken이 유효하지 않음. errorCode: UNAUTHORIZED",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 도감 항목. errorCode: CATALOG_ITEM_NOT_FOUND",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "409", description = "이미 수집한 도감 항목. errorCode: CATALOG_ITEM_ALREADY_COLLECTED",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<Void> collectCatalogItem(UUID memberId, UUID catalogItemId);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2026-07-13",
          author = ChangeLogAuthor.KIM_JAEHYEON,
          description = "도감 수집 현황 요약 API 최초 작성"
      )
  })
  @Operation(
      summary = "도감 수집 현황 요약",
      description = """
          전체 및 카테고리별(ANIMAL/PLANT/PLACE) 도감 항목 총 개수, 현재 회원의 수집 개수,
          수집률(정수 퍼센트, 0~100, 서버가 계산해 그대로 내려줌)을 반환한다.

          - 홈 화면의 진행률 카드, 내 정보의 통계 카드 등 개수만 필요한 화면에서 사용한다.
          - collectionRate는 반올림된 정수 퍼센트다(앱에서 별도로 계산하지 않고 그대로 표시).
          - 아직 동기화된 항목이 전혀 없는 카테고리(totalCount=0)는 collectionRate도 0으로 내려간다.
          - Authorization: Bearer {accessToken} 헤더가 반드시 필요하다.
          """,
      security = @SecurityRequirement(name = "Bearer Token")
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "401", description = "Authorization 헤더가 없거나 accessToken이 유효하지 않음. errorCode: UNAUTHORIZED",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<CatalogSummaryResponse> getCatalogSummary(UUID memberId);
}
