package com.chuseok22.eodaegoserver.domain.catalog.controller;

import com.chuseok22.apichangelog.annotation.ApiChangeLog;
import com.chuseok22.apichangelog.annotation.ApiChangeLogs;
import com.chuseok22.eodaegoserver.domain.catalog.dto.request.CatalogItemStatusUpdateRequest;
import com.chuseok22.eodaegoserver.domain.catalog.dto.request.CatalogItemUpdateRequest;
import com.chuseok22.eodaegoserver.domain.catalog.dto.response.CatalogItemResponse;
import com.chuseok22.eodaegoserver.domain.catalog.dto.response.CatalogSyncResponse;
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

@Tag(name = "Catalog Item", description = "도감 항목 데이터 관리 API")
public interface CatalogItemControllerDocs {

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2026-07-12",
          author = ChangeLogAuthor.KIM_JAEHYEON,
          description = "도감 항목 관리 API 최초 작성",
          issueUrl = "https://github.com/eodaego/eodaego-BE/issues/12"
      )
  })
  @Operation(
      summary = "도감 항목 전체 목록 조회(관리용)",
      description = """
          회원의 수집 여부와 무관하게 CatalogItem 원본 데이터를 그대로 전체 반환한다.

          - Authorization: Bearer {accessToken} 헤더가 반드시 필요하다.
          """,
      security = @SecurityRequirement(name = "Bearer Token")
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "401", description = "Authorization 헤더가 없거나 accessToken이 유효하지 않음. errorCode: UNAUTHORIZED",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<List<CatalogItemResponse>> getCatalogItems();

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2026-07-12",
          author = ChangeLogAuthor.KIM_JAEHYEON,
          description = "도감 항목 관리 API 최초 작성",
          issueUrl = "https://github.com/eodaego/eodaego-BE/issues/12"
      )
  })
  @Operation(
      summary = "도감 항목 단건 조회(관리용)",
      description = """
          회원의 수집 여부와 무관하게 CatalogItem 원본 데이터를 그대로 반환한다.

          - Authorization: Bearer {accessToken} 헤더가 반드시 필요하다.
          """,
      security = @SecurityRequirement(name = "Bearer Token")
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "401", description = "Authorization 헤더가 없거나 accessToken이 유효하지 않음. errorCode: UNAUTHORIZED",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 도감 항목. errorCode: CATALOG_ITEM_NOT_FOUND",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<CatalogItemResponse> getCatalogItem(UUID catalogItemId);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2026-07-12",
          author = ChangeLogAuthor.KIM_JAEHYEON,
          description = "도감 항목 관리 API 최초 작성",
          issueUrl = "https://github.com/eodaego/eodaego-BE/issues/12"
      ),
      @ApiChangeLog(
          date = "2026-07-13",
          author = ChangeLogAuthor.KIM_JAEHYEON,
          description = "이미 등록된 항목도 외부 원본 값(이름/이미지 또는 위경도)이 바뀌었으면 갱신하도록 변경. 응답을 신규 등록 목록/갱신 목록으로 분리(CatalogSyncResponse)"
      )
  })
  @Operation(
      summary = "외부 AI 서버 전체 동기화",
      description = """
          외부 AI 서버(동물/식물/시설)의 전체 목록을 조회해, 아직 등록되지 않은(externalId 기준) 항목은
          신규 등록하고, 이미 등록된 항목은 외부 원본 값이 바뀌었을 때만 갱신한다.

          - 신규 등록된 항목은 feature/childDescription이 빈 문자열("")로 생성되며(식물은 feature가 외부
            설명으로 자동 채워짐), 관리자가 이후 별도로 채워야 한다.
          - status는 AVAILABLE로 기본 생성된다.
          - 이미 등록된 항목(externalId 기준)은 새로 만들지 않는다. 대신 동물/식물은 name/imageUrl,
            시설은 name/latitude/longitude가 외부 원본과 다르면 그 값만 갱신한다.
          - feature/childDescription/status는 관리자가 직접 입력한 값이라 재동기화로 절대 덮어쓰지 않는다.
          - Authorization: Bearer {accessToken} 헤더가 반드시 필요하다.
          """,
      security = @SecurityRequirement(name = "Bearer Token")
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "동기화 성공. created(신규 등록된 항목 목록)와 updated(값이 바뀌어 갱신된 항목 목록)를 함께 반환한다(둘 다 없으면 빈 배열)"),
      @ApiResponse(responseCode = "401", description = "Authorization 헤더가 없거나 accessToken이 유효하지 않음. errorCode: UNAUTHORIZED",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "503", description = "외부 AI 서버 호출 실패. errorCode: AI_SERVER_UNAVAILABLE",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<CatalogSyncResponse> syncCatalogItems();

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2026-07-12",
          author = ChangeLogAuthor.KIM_JAEHYEON,
          description = "도감 항목 관리 API 최초 작성",
          issueUrl = "https://github.com/eodaego/eodaego-BE/issues/12"
      )
  })
  @Operation(
      summary = "도감 항목 통합 수정",
      description = """
          name/feature/childDescription/status/imageUrl/latitude/longitude를 한 번에 수정한다.
          category/externalId는 등록 이후 변경할 수 없어 이 요청에 포함되지 않는다.

          - Authorization: Bearer {accessToken} 헤더가 반드시 필요하다.
          """,
      security = @SecurityRequirement(name = "Bearer Token")
  )
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "수정 성공"),
      @ApiResponse(responseCode = "400", description = "요청 값 검증 실패(name/feature/childDescription 빈 값, status 누락 등). errorCode: INVALID_REQUEST",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "401", description = "Authorization 헤더가 없거나 accessToken이 유효하지 않음. errorCode: UNAUTHORIZED",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 도감 항목. errorCode: CATALOG_ITEM_NOT_FOUND",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<Void> updateCatalogItem(UUID catalogItemId, CatalogItemUpdateRequest request);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2026-07-12",
          author = ChangeLogAuthor.KIM_JAEHYEON,
          description = "도감 항목 관리 API 최초 작성",
          issueUrl = "https://github.com/eodaego/eodaego-BE/issues/12"
      )
  })
  @Operation(
      summary = "도감 항목 상태만 변경",
      description = """
          status(AVAILABLE/SUSPENDED/RETIRED)만 단독으로 변경한다.

          - Authorization: Bearer {accessToken} 헤더가 반드시 필요하다.
          """,
      security = @SecurityRequirement(name = "Bearer Token")
  )
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "변경 성공"),
      @ApiResponse(responseCode = "400", description = "status 누락. errorCode: INVALID_REQUEST",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "401", description = "Authorization 헤더가 없거나 accessToken이 유효하지 않음. errorCode: UNAUTHORIZED",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 도감 항목. errorCode: CATALOG_ITEM_NOT_FOUND",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<Void> updateCatalogItemStatus(UUID catalogItemId, CatalogItemStatusUpdateRequest request);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2026-07-12",
          author = ChangeLogAuthor.KIM_JAEHYEON,
          description = "도감 항목 관리 API 최초 작성",
          issueUrl = "https://github.com/eodaego/eodaego-BE/issues/12"
      )
  })
  @Operation(
      summary = "도감 항목 삭제",
      description = """
          Authorization: Bearer {accessToken} 헤더가 반드시 필요하다.
          """,
      security = @SecurityRequirement(name = "Bearer Token")
  )
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "삭제 성공"),
      @ApiResponse(responseCode = "401", description = "Authorization 헤더가 없거나 accessToken이 유효하지 않음. errorCode: UNAUTHORIZED",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 도감 항목. errorCode: CATALOG_ITEM_NOT_FOUND",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<Void> deleteCatalogItem(UUID catalogItemId);
}
