package com.chuseok22.eodaegoserver.domain.quiz.service;

import com.chuseok22.eodaegoserver.global.properties.QuizProperties;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(QuizProperties.class)
public class QuizAnswerStore {

  private static final String KEY_PREFIX = "quiz:answer:";

  private final StringRedisTemplate redisTemplate;
  private final QuizProperties quizProperties;

  public void save(UUID quizId, UUID memberId, UUID correctCatalogItemId) {
    redisTemplate.opsForValue()
        .set(key(quizId), memberId + ":" + correctCatalogItemId, Duration.ofMillis(quizProperties.answerTtlMillis()));
    log.debug("퀴즈 정답 저장. quizId={}, ttlMillis={}", quizId, quizProperties.answerTtlMillis());
  }

  public Optional<QuizAnswer> find(UUID quizId) {
    String value = redisTemplate.opsForValue().get(key(quizId));
    if (value == null) {
      return Optional.empty();
    }
    String[] parts = value.split(":");
    return Optional.of(new QuizAnswer(UUID.fromString(parts[0]), UUID.fromString(parts[1])));
  }

  public void delete(UUID quizId) {
    redisTemplate.delete(key(quizId));
  }

  private String key(UUID quizId) {
    return KEY_PREFIX + quizId;
  }

  public record QuizAnswer(UUID memberId, UUID correctCatalogItemId) {
  }
}
