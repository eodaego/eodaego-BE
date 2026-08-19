package com.chuseok22.eodaegoserver.domain.quiz.service;

import com.chuseok22.eodaegoserver.domain.catalog.CatalogCategory;
import com.chuseok22.eodaegoserver.domain.catalog.CatalogItemStatus;
import com.chuseok22.eodaegoserver.domain.catalog.entity.CatalogItem;
import com.chuseok22.eodaegoserver.domain.catalog.repository.CatalogItemRepository;
import com.chuseok22.eodaegoserver.domain.catalog.repository.MemberCatalogCollectionRepository;
import com.chuseok22.eodaegoserver.domain.quiz.dto.external.AiPhotoRecognitionResponse;
import com.chuseok22.eodaegoserver.domain.quiz.dto.request.QuizAnswerRequest;
import com.chuseok22.eodaegoserver.domain.quiz.dto.response.QuizAnswerResponse;
import com.chuseok22.eodaegoserver.domain.quiz.dto.response.QuizChoiceResponse;
import com.chuseok22.eodaegoserver.domain.quiz.dto.response.QuizResponse;
import com.chuseok22.eodaegoserver.domain.quiz.service.QuizAnswerStore.QuizAnswer;
import com.chuseok22.eodaegoserver.global.exception.CustomException;
import com.chuseok22.eodaegoserver.global.exception.ErrorCode;
import com.chuseok22.eodaegoserver.global.properties.QuizProperties;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
  private final QuizProperties quizProperties;
  private final Clock clock;


  public QuizResponse generateQuiz(UUID memberId, CatalogCategory catalogType, MultipartFile image) {
    validateImage(memberId, image);

    AiPhotoRecognitionResponse recognition = quizAiClient.identify(catalogType, image);
    if (recognition.catalogId() == null) {
      log.warn("사진 인식 실패. memberId={}, catalogType={}", memberId, catalogType);
      throw new CustomException(ErrorCode.RECOGNITION_FAILED);
    }

    CatalogItem correctItem = findAvailableItem(catalogType, recognition);

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

    // 정답 기록은 여기서 지우지 않고 TTL로 만료시킨다. 즉시 지우면 응답이 유실된 뒤의 재시도가
    // QUIZ_NOT_FOUND(404)를 받는다. 중복 수집은 insertIfAbsent가 DB 유니크 제약으로 막는다.
    int inserted = memberCatalogCollectionRepository
        .insertIfAbsent(memberId, correctCatalogItemId, LocalDateTime.now(clock));
    if (inserted == 0) {
      log.info("퀴즈 정답. 이미 수집한 항목이라 획득 스킵. memberId={}, catalogItemId={}", memberId, correctCatalogItemId);
    } else {
      log.info("퀴즈 정답. 도감 획득 완료. memberId={}, catalogItemId={}", memberId, correctCatalogItemId);
    }

    return QuizAnswerResponse.correct(correctCatalogItemId);
  }

  private void validateImage(UUID memberId, MultipartFile image) {
    if (image.isEmpty()) {
      log.warn("퀴즈 생성 실패. 이미지가 비어 있음. memberId={}", memberId);
      throw new CustomException(ErrorCode.INVALID_REQUEST);
    }

    String contentType = image.getContentType();
    if (contentType == null || !contentType.startsWith("image/")) {
      log.warn("퀴즈 생성 실패. 이미지 파일이 아님. memberId={}, contentType={}, size={}",
          memberId, contentType, image.getSize());
      throw new CustomException(ErrorCode.INVALID_IMAGE_FORMAT);
    }

    long maxImageSizeBytes = quizProperties.maxImageSize().toBytes();
    if (image.getSize() > maxImageSizeBytes) {
      log.warn("퀴즈 생성 실패. 이미지 크기 초과. memberId={}, size={}, limit={}",
          memberId, image.getSize(), maxImageSizeBytes);
      throw new CustomException(ErrorCode.IMAGE_TOO_LARGE);
    }
  }

  private CatalogItem findAvailableItem(CatalogCategory catalogType, AiPhotoRecognitionResponse recognition) {
    Long catalogId = recognition.catalogId();

    Optional<CatalogItem> found = catalogType == CatalogCategory.PLACE
        ? catalogItemRepository.findByCategoryAndStatusAndFacility_AiFacilityId(
        catalogType, CatalogItemStatus.AVAILABLE, catalogId)
        : catalogItemRepository.findByCategoryAndStatusAndSource_ExternalId(
            catalogType, CatalogItemStatus.AVAILABLE, catalogId);

    return found.orElseThrow(() -> {
      log.warn("인식된 대상이 도감에 없거나 수집 가능 상태가 아님. catalogType={}, catalogId={}, aiName={}",
          catalogType, catalogId, recognition.name());
      return new CustomException(ErrorCode.RECOGNITION_FAILED);
    });
  }

  private List<QuizChoiceResponse> buildChoices(CatalogCategory category, CatalogItem correctItem) {
    List<UUID> distractorIds =
        catalogItemRepository.findRandomIdsByCategoryExcluding(category.name(), correctItem.getId(), CHOICE_COUNT - 1);
    List<CatalogItem> chosen = new ArrayList<>(catalogItemRepository.findAllById(distractorIds));
    chosen.add(correctItem);
    Collections.shuffle(chosen);

    return chosen.stream().map(QuizChoiceResponse::from).toList();
  }
}
