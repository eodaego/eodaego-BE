package com.chuseok22.eodaegoserver.domain.quiz.controller;

import com.chuseok22.apichangelog.annotation.ApiChangeLog;
import com.chuseok22.apichangelog.annotation.ApiChangeLogs;
import com.chuseok22.eodaegoserver.domain.catalog.CatalogCategory;
import com.chuseok22.eodaegoserver.domain.quiz.dto.request.QuizAnswerRequest;
import com.chuseok22.eodaegoserver.domain.quiz.dto.response.QuizAnswerResponse;
import com.chuseok22.eodaegoserver.domain.quiz.dto.response.QuizResponse;
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
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Quiz", description = "사진 인식 기반 퀴즈 API")
public interface QuizControllerDocs {

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2026-08-03",
          author = ChangeLogAuthor.KIM_JAEHYEON,
          description = "사진 인식 기반 퀴즈 생성 API 최초 작성",
          issueUrl = "https://github.com/eodaego/eodaego-BE/issues/39"
      ),
      @ApiChangeLog(
          date = "2026-08-08",
          author = ChangeLogAuthor.KIM_JAEHYEON,
          description = "수집 가능 상태(AVAILABLE)가 아닌 항목은 퀴즈로 출제되지 않도록 변경. 관리자가 SUSPENDED/RETIRED로 내려둔 항목이 퀴즈를 통해 수집되던 문제 수정",
          issueUrl = "https://github.com/eodaego/eodaego-BE/issues/39"
      )
  })
  @Operation(
      summary = "사진 인식 + 퀴즈 생성",
      description = """
          촬영한 사진을 AI로 인식해 같은 종류의 3지선다 퀴즈를 생성한다.

          - multipart/form-data로 요청한다. catalogType(ANIMAL/PLANT/PLACE)은 사용자가 촬영 전에 고른 대상 종류이고, image는 촬영한 사진 파일이다.
          - 서버가 사진을 인식해 도감 항목을 찾고, 같은 카테고리에서 오답 2개를 뽑아 정답 1개와 함께 무작위 순서로 3개의 선택지를 반환한다.
          - 정답 정보는 응답에 포함되지 않는다(서버가 보관). 응답의 quizId를 정답 제출 API에 그대로 전달한다.
          - 출제 대상은 status가 AVAILABLE인 항목뿐이다. 관리자가 SUSPENDED(임시 중단)나 RETIRED(운영 종료)로 내려둔 항목은
            사진 인식에 성공하더라도 퀴즈가 생성되지 않으며, 따라서 퀴즈로 수집할 수도 없다.
          - 사진을 인식하지 못했거나, 인식된 대상이 도감에 없거나, 수집 가능 상태가 아니면 422(RECOGNITION_FAILED)를 반환한다.
          - Authorization: Bearer {accessToken} 헤더가 반드시 필요하다.
          """,
      security = @SecurityRequirement(name = "Bearer Token")
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "퀴즈 생성 성공"),
      @ApiResponse(responseCode = "401", description = "Authorization 헤더가 없거나 accessToken이 유효하지 않음. errorCode: UNAUTHORIZED",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "422", description = "사진을 인식하지 못했거나, 인식된 대상이 도감에 없거나, 수집 가능 상태(AVAILABLE)가 아님. errorCode: RECOGNITION_FAILED",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "503", description = "AI 서버 호출 실패(연결 오류/타임아웃 등). errorCode: AI_SERVER_UNAVAILABLE",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<QuizResponse> generateQuiz(UUID memberId, CatalogCategory catalogType, MultipartFile image);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2026-08-03",
          author = ChangeLogAuthor.KIM_JAEHYEON,
          description = "퀴즈 정답 제출 API 최초 작성",
          issueUrl = "https://github.com/eodaego/eodaego-BE/issues/39"
      ),
      @ApiChangeLog(
          date = "2026-08-16",
          author = ChangeLogAuthor.KIM_JAEHYEON,
          description = "같은 quizId로 정답을 여러 번 제출해도 항상 같은 200 응답을 반환하도록 변경(멱등). 기존에는 더블탭 시 한 요청이 409로 실패하고, 응답이 유실된 뒤 재시도하면 404 QUIZ_NOT_FOUND가 났다. 정답 처리 경로에서 CATALOG_ITEM_NOT_FOUND / MEMBER_NOT_FOUND를 더 이상 반환하지 않으며, 해당 상황은 409 DATA_INTEGRITY_VIOLATION으로 내려간다",
          issueUrl = "https://github.com/eodaego/eodaego-BE/issues/39"
      )
  })
  @Operation(
      summary = "퀴즈 정답 제출",
      description = """
          생성된 퀴즈에 대해 선택한 답을 제출한다. 대상 퀴즈는 경로 변수 quizId로 지정하고, 고른 선택지는 요청 바디의 selectedCatalogItemId로 전달한다.

          - 오답도 에러가 아닌 200 정상 응답으로 내려간다(correct=false). 같은 quizId로 다른 선택지를 다시 제출할 수 있다(재시도 가능).
          - 정답이면 해당 도감 항목을 수집 처리하고(이미 수집한 항목이면 획득은 스킵) correct=true와 collectedCatalogItemId를 반환한다.
          - 정답 제출은 멱등하다. 더블탭으로 동시에 보내든, 응답이 유실돼 재시도하든 같은 200 응답을 받으며 수집은 1회만 기록된다.
          - 정답 제출 후에도 quizId는 만료(30분) 전까지 유효하다. 오답과 동일하게 재제출이 가능하며, 중복 수집은 발생하지 않는다.
          - 만료되었거나 존재하지 않는 quizId이거나, 퀴즈를 생성한 회원 본인이 아니면 404(QUIZ_NOT_FOUND)를 반환한다.
          - Authorization: Bearer {accessToken} 헤더가 반드시 필요하다.
          """,
      security = @SecurityRequirement(name = "Bearer Token")
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "제출 처리 성공(정답/오답 모두 200으로 내려감)"),
      @ApiResponse(responseCode = "400", description = "quizId 또는 selectedCatalogItemId 누락 등 요청 검증 실패. errorCode: INVALID_REQUEST",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "401", description = "Authorization 헤더가 없거나 accessToken이 유효하지 않음. errorCode: UNAUTHORIZED",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "404", description = "만료/미존재 quizId이거나 퀴즈를 생성한 회원 본인이 아님. errorCode: QUIZ_NOT_FOUND",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "409", description = "정답 처리 중 대상 도감 항목이 삭제되었거나 회원이 존재하지 않아 수집 기록을 남길 수 없음. errorCode: DATA_INTEGRITY_VIOLATION",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<QuizAnswerResponse> submitAnswer(UUID memberId, UUID quizId, QuizAnswerRequest request);
}
