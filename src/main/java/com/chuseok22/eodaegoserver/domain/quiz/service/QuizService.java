package com.chuseok22.eodaegoserver.domain.quiz.service;

import com.chuseok22.eodaegoserver.domain.catalog.CatalogCategory;
import com.chuseok22.eodaegoserver.domain.catalog.entity.CatalogItem;
import com.chuseok22.eodaegoserver.domain.catalog.entity.MemberCatalogCollection;
import com.chuseok22.eodaegoserver.domain.catalog.repository.CatalogItemRepository;
import com.chuseok22.eodaegoserver.domain.catalog.repository.MemberCatalogCollectionRepository;
import com.chuseok22.eodaegoserver.domain.member.entity.Member;
import com.chuseok22.eodaegoserver.domain.member.repository.MemberRepository;
import com.chuseok22.eodaegoserver.domain.quiz.dto.external.AiPhotoRecognitionResponse;
import com.chuseok22.eodaegoserver.domain.quiz.dto.request.QuizAnswerRequest;
import com.chuseok22.eodaegoserver.domain.quiz.dto.response.QuizAnswerResponse;
import com.chuseok22.eodaegoserver.domain.quiz.dto.response.QuizChoiceResponse;
import com.chuseok22.eodaegoserver.domain.quiz.dto.response.QuizResponse;
import com.chuseok22.eodaegoserver.domain.quiz.service.QuizAnswerStore.QuizAnswer;
import com.chuseok22.eodaegoserver.global.exception.CustomException;
import com.chuseok22.eodaegoserver.global.exception.ErrorCode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService {

  private static final int CHOICE_COUNT = 3;

  private final QuizAiClient quizAiClient;
  private final QuizAnswerStore quizAnswerStore;
  private final CatalogItemRepository catalogItemRepository;
  private final MemberCatalogCollectionRepository memberCatalogCollectionRepository;
  private final MemberRepository memberRepository;
  private final Clock clock;


  public QuizResponse generateQuiz(UUID memberId, CatalogCategory catalogType, MultipartFile image) {
    AiPhotoRecognitionResponse recognition = quizAiClient.identify(catalogType, image);
    if (recognition.catalogId() == null) {
      log.warn("사진 인식 실패. memberId={}, catalogType={}", memberId, catalogType);
      throw new CustomException(ErrorCode.RECOGNITION_FAILED);
    }

    CatalogItem correctItem = catalogItemRepository
        .findByCategoryAndSource_ExternalId(catalogType, recognition.catalogId())
        .orElseThrow(() -> {
          log.warn("인식된 대상이 도감에 없음. catalogType={}, externalId={}, aiName={}", catalogType, recognition.catalogId(), recognition.name());
          return new CustomException(ErrorCode.RECOGNITION_FAILED);
        });

    List<QuizChoiceResponse> choices = buildChoices(catalogType, correctItem);

    UUID quizId = UUID.randomUUID();
    quizAnswerStore.save(quizId, memberId, correctItem.getId());
    log.info("퀴즈 생성. memberId={}, quizId={}, catalogType={}, answerItemId={}", memberId, quizId, catalogType, correctItem.getId());

    return QuizResponse.of(quizId, choices);
  }

  @Transactional
  public QuizAnswerResponse submitAnswer(UUID memberId, UUID quizId, QuizAnswerRequest request) {
    QuizAnswer quizAnswer = quizAnswerStore.find(quizId)
        .filter(answer -> answer.memberId().equals(memberId))
        .orElseThrow(() -> {
          log.warn("퀴즈 조회 실패. 만료되었거나 존재하지 않음. memberId={}, quizId={}", memberId, quizId);
          return new CustomException(ErrorCode.QUIZ_NOT_FOUND);
        });

    UUID correctCatalogItemId = quizAnswer.correctCatalogItemId();

    if (!correctCatalogItemId.equals(request.selectedCatalogItemId())) {
      log.info("퀴즈 오답. memberId={}, quizId={}", memberId, quizId);
      return QuizAnswerResponse.wrong();
    }

    boolean alreadyCollected = memberCatalogCollectionRepository
        .findByMemberIdAndCatalogItemId(memberId, correctCatalogItemId)
        .isPresent();
    if (!alreadyCollected) {
      CatalogItem catalogItem = catalogItemRepository.findById(correctCatalogItemId)
          .orElseThrow(() -> new CustomException(ErrorCode.CATALOG_ITEM_NOT_FOUND));
      Member member = memberRepository.findById(memberId)
          .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
      memberCatalogCollectionRepository.save(MemberCatalogCollection.builder()
          .member(member)
          .catalogItem(catalogItem)
          .collectedAt(LocalDateTime.now(clock))
          .build());
      log.info("퀴즈 정답. 도감 획득 완료. memberId={}, catalogItemId={}", memberId, correctCatalogItemId);
    } else {
      log.info("퀴즈 정답. 이미 수집한 항목이라 획득 스킵. memberId={}, catalogItemId={}", memberId, correctCatalogItemId);
    }

    quizAnswerStore.delete(quizId);
    return QuizAnswerResponse.correct(correctCatalogItemId);
  }

  private List<QuizChoiceResponse> buildChoices(CatalogCategory category, CatalogItem correctItem) {
    List<UUID> distractorIds =
        catalogItemRepository.findRandomDistractorIds(category.name(), correctItem.getId(), CHOICE_COUNT - 1);
    List<CatalogItem> chosen = new ArrayList<>(catalogItemRepository.findAllById(distractorIds));
    chosen.add(correctItem);
    Collections.shuffle(chosen);

    return chosen.stream().map(QuizChoiceResponse::from).toList();
  }
}
