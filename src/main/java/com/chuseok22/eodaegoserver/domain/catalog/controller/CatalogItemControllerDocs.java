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
      ),
      @ApiChangeLog(
          date = "2026-08-04",
          author = ChangeLogAuthor.KIM_JAEHYEON,
          description = "목록 정렬 기준이 없어 항목 순서가 보장되지 않던 문제 수정. 이제 카테고리(ANIMAL → PLANT → PLACE) → sequenceNumber 오름차순으로 반환된다",
          issueUrl = "https://github.com/eodaego/eodaego-BE/issues/58"
      )
  })
  @Operation(
      summary = "도감 항목 전체 목록 조회(관리용)",
      description = """
          회원의 수집 여부와 무관하게 CatalogItem 원본 데이터를 그대로 전체 반환한다.

          - 카테고리(ANIMAL → PLANT → PLACE, 즉 도감 코드 접두사 A → B → C 순)로 먼저 묶인 뒤, 각 카테고리 안에서 sequenceNumber 오름차순으로 정렬되어 내려간다.
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
      ),
      @ApiChangeLog(
          date = "2026-07-16",
          author = ChangeLogAuthor.KIM_JAEHYEON,
          description = "PLACE(시설) 신규 등록 시 feature가 항상 빈 문자열로 저장되던 문제 수정. 외부 description(상세 설명)을 feature로 자동 매핑하도록 변경(식물과 동일한 방식)",
          issueUrl = "https://github.com/eodaego/eodaego-BE/issues/30"
      ),
      @ApiChangeLog(
          date = "2026-08-04",
          author = ChangeLogAuthor.KIM_JAEHYEON,
          description = "시설 중 출입문(정문/후문 등 11개)과 고객안내센터를 도감 등록 대상에서 제외. 동기화해도 PLACE로 생성되지 않으며, 기존에 등록돼 있던 12건은 삭제됨",
          issueUrl = "https://github.com/eodaego/eodaego-BE/issues/52"
      ),
      @ApiChangeLog(
          date = "2026-08-04",
          author = ChangeLogAuthor.KIM_JAEHYEON,
          description = "재동기화가 관리자 수정값(name/imageUrl/위경도)을 AI 원본으로 되돌리던 문제 수정. 이제 AI 원본은 별도로 보관되고, 관리자가 수정하지 않은 필드만 AI 변경사항을 반영한다. 함께 수정되지 않던 식물/시설의 feature도 이제 갱신된다",
          issueUrl = "https://github.com/eodaego/eodaego-BE/issues/60"
      )
  })
  @Operation(
      summary = "외부 AI 서버 전체 동기화",
      description = """
          외부 AI 서버(동물/식물/시설)의 전체 목록을 조회해 AI 원본 데이터를 갱신한다.
          아직 등록되지 않은(카테고리 + externalId 기준) 항목은 도감 항목까지 함께 신규 등록한다.

          - **관리자가 수정한 값은 이 API로 절대 덮어쓰이지 않는다.** AI 원본과 관리자 수정값이
            분리 보관되므로, 수정해둔 필드는 몇 번을 동기화해도 그대로 유지된다.
          - 반대로 관리자가 수정하지 않은 필드는 AI 원본이 바뀌면 그대로 따라간다.
            예를 들어 이름만 수정해둔 항목은, 이름은 유지되면서 이미지·설명은 AI 최신값으로 갱신된다.
          - 신규 등록된 항목은 childDescription이 빈 문자열("")로 생성되며 관리자가 직접 채워야 한다.
            AI가 childDescription을 제공하지 않기 때문이다.
          - name/feature/imageUrl/latitude/longitude는 관리자가 수정하기 전까지 AI 원본 값이 그대로 표시된다.
          - status는 AVAILABLE로 기본 생성되며, 재동기화로 바뀌지 않는다.
          - 시설 중 출입문(정문/후문 등)과 고객안내센터는 회원이 수집하는 도감 대상이 아니므로
            동기화에서 제외된다. 즉 시설 목록 전체가 PLACE로 등록되지는 않는다.
          - Authorization: Bearer {accessToken} 헤더가 반드시 필요하다.
          """,
      security = @SecurityRequirement(name = "Bearer Token")
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "동기화 성공. created(신규 등록된 항목 목록)와 updated(AI 원본 값이 바뀐 항목 목록)를 함께 반환한다(둘 다 없으면 빈 배열). updated에 포함되어도 관리자가 수정해둔 필드는 바뀌지 않는다"),
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
      ),
      @ApiChangeLog(
          date = "2026-08-04",
          author = ChangeLogAuthor.KIM_JAEHYEON,
          description = "요청 필드명이 name/feature/imageUrl/latitude/longitude에서 nameOverride/featureOverride/imageUrlOverride/latitudeOverride/longitudeOverride로 변경됨. null을 보내면 해당 필드가 AI 원본 값으로 되돌아간다(기존에는 name/feature가 필수라 원복이 불가능했음)",
          issueUrl = "https://github.com/eodaego/eodaego-BE/issues/60"
      ),
      @ApiChangeLog(
          date = "2026-08-06",
          author = ChangeLogAuthor.KIM_JAEHYEON,
          description = "childDescription 검증을 @NotBlank에서 @NotNull로 완화. 동기화는 childDescription을 빈 문자열로 생성하는데 수정 API만 빈 문자열을 거부해, 아직 설명을 작성하지 않은 항목의 이름·이미지만 고치는 것이 불가능했다. 이제 빈 문자열을 보낼 수 있다(필드 자체는 여전히 필수)",
          issueUrl = "https://github.com/eodaego/eodaego-BE/issues/62"
      )
  })
  @Operation(
      summary = "도감 항목 통합 수정",
      description = """
          AI 원본 대신 사용할 값(xxxOverride)과 관리자 전용 값(childDescription/status)을 한 번에 수정한다.
          category/externalId는 등록 이후 변경할 수 없어 이 요청에 포함되지 않는다.

          - **xxxOverride에 null을 보내면 그 필드는 AI 원본 값을 따른다(원복).** 값을 보내면 그 값이
            AI 원본 대신 표시되며, 이후 동기화로 덮어쓰이지 않는다.
          - 예를 들어 nameOverride만 채우고 imageUrlOverride를 null로 두면, 이름은 지정한 값으로 고정되고
            이미지는 AI가 바꿀 때마다 최신값을 따라간다.
          - childDescription과 status는 AI가 제공하지 않는 관리자 전용 값이라 원복 개념이 없고 필드 자체는 필수다.
            다만 childDescription은 아직 작성 전이면 빈 문자열("")을 보내면 된다. 동기화로 생성된 항목도
            빈 문자열로 시작하므로, 설명을 쓰지 않은 상태에서 이름·이미지만 수정할 수 있다.
          - 이 API는 전체 교체 방식이다. 생략한 xxxOverride는 "유지"가 아니라 "null로 설정(원복)"으로
            처리되므로, 유지하려는 override 값도 함께 보내야 한다.
          - Authorization: Bearer {accessToken} 헤더가 반드시 필요하다.
          """,
      security = @SecurityRequirement(name = "Bearer Token")
  )
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "수정 성공"),
      @ApiResponse(responseCode = "400", description = "요청 값 검증 실패(childDescription 누락, status 누락 등). errorCode: INVALID_REQUEST",
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
      ),
      @ApiChangeLog(
          date = "2026-08-04",
          author = ChangeLogAuthor.KIM_JAEHYEON,
          description = "도감 항목과 함께 그 AI 원본도 삭제하도록 변경. 원본만 남으면 다음 동기화가 '이미 있는 항목'으로 판단해 삭제한 항목이 되살아나지 않는 문제가 있었다",
          issueUrl = "https://github.com/eodaego/eodaego-BE/issues/60"
      )
  })
  @Operation(
      summary = "도감 항목 삭제",
      description = """
          도감 항목과 그 AI 원본 데이터를 함께 삭제한다.
          따라서 다음 동기화 때 AI 서버가 같은 항목을 보내주면 새 항목으로 다시 등록된다
          (관리자가 입력했던 childDescription과 override 값은 함께 사라진다).

          - Authorization: Bearer {accessToken} 헤더가 반드시 필요하다.
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
