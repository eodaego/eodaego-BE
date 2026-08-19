package com.chuseok22.eodaegoserver.domain.quiz.controller;

import com.chuseok22.eodaegoserver.domain.catalog.CatalogCategory;
import com.chuseok22.eodaegoserver.domain.quiz.dto.request.QuizAnswerRequest;
import com.chuseok22.eodaegoserver.domain.quiz.dto.response.QuizAnswerResponse;
import com.chuseok22.eodaegoserver.domain.quiz.dto.response.QuizResponse;
import com.chuseok22.eodaegoserver.domain.quiz.service.QuizService;
import com.chuseok22.logging.annotation.LogMonitoring;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/quiz")
@RequiredArgsConstructor
public class QuizController implements QuizControllerDocs {

  private final QuizService quizService;

  @Override
  @LogMonitoring
  @PostMapping(path = "", version = "1", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<QuizResponse> generateQuiz(
      @AuthenticationPrincipal UUID memberId,
      @RequestParam CatalogCategory catalogType,
      @RequestPart MultipartFile image) {
    return ResponseEntity.ok(quizService.generateQuiz(memberId, catalogType, image));
  }

  @Override
  @LogMonitoring
  @PostMapping(path = "/{quizId}/answer", version = "1")
  public ResponseEntity<QuizAnswerResponse> submitAnswer(
      @AuthenticationPrincipal UUID memberId,
      @PathVariable UUID quizId,
      @Valid @RequestBody QuizAnswerRequest request) {
    return ResponseEntity.ok(quizService.submitAnswer(memberId, quizId, request));
  }
}
