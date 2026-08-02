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
      )
  })
  @Operation(
      summary = "사진 인식 + 퀴즈 생성",
      description = """
          촬영한 사진을 AI로 인식해 같은 종류의 3지선다 퀴즈를 생성한다.

          - multipart/form-data로 요청한다. catalogType(ANIMAL/PLANT/PLACE)은 사용자가 촬영 전에 고른 대상 종류이고, image는 촬영한 사진 파일이다.
          - 서버가 사진을 인식해 도감 항목을 찾고, 같은 카테고리에서 오답 2개를 뽑아 정답 1개와 함께 무작위 순서로 3개의 선택지를 반환한다.
          - 정답 정보는 응답에 포함되지 않는다(서버가 보관). 응답의 quizId를 정답 제출 API에 그대로 전달한다.
          - 사진을 인식하지 못했거나 인식된 대상이 도감에 없으면 422(RECOGNITION_FAILED)를 반환한다.
          - Authorization: Bearer {accessToken} 헤더가 반드시 필요하다.
          """,
      security = @SecurityRequirement(name = "Bearer Token")
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "퀴즈 생성 성공"),
      @ApiResponse(responseCode = "401", description = "Authorization 헤더가 없거나 accessToken이 유효하지 않음. errorCode: UNAUTHORIZED",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "422", description = "사진을 인식하지 못했거나 인식된 대상이 도감에 없음. errorCode: RECOGNITION_FAILED",
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
      )
  })
  @Operation(
      summary = "퀴즈 정답 제출",
      description = """
          생성된 퀴즈에 대해 선택한 답을 제출한다. 대상 퀴즈는 경로 변수 quizId로 지정하고, 고른 선택지는 요청 바디의 selectedCatalogItemId로 전달한다.

          - 오답도 에러가 아닌 200 정상 응답으로 내려간다(correct=false). 같은 quizId로 다른 선택지를 다시 제출할 수 있다(재시도 가능).
          - 정답이면 해당 도감 항목을 수집 처리하고(이미 수집한 항목이면 획득은 스킵) correct=true와 collectedCatalogItemId를 반환한다.
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
      @ApiResponse(responseCode = "404", description = """
          다음 중 하나:
          - QUIZ_NOT_FOUND: 만료/미존재 quizId이거나 퀴즈를 생성한 회원 본인이 아님
          - CATALOG_ITEM_NOT_FOUND: 정답 항목이 도감에서 삭제된 경우
          - MEMBER_NOT_FOUND: 존재하지 않는 회원""",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<QuizAnswerResponse> submitAnswer(UUID memberId, UUID quizId, QuizAnswerRequest request);
}
