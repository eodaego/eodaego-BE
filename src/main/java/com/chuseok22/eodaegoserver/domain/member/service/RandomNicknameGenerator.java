package com.chuseok22.eodaegoserver.domain.member.service;

import com.chuseok22.eodaegoserver.domain.member.repository.MemberRepository;
import com.chuseok22.eodaegoserver.global.exception.CustomException;
import com.chuseok22.eodaegoserver.global.exception.ErrorCode;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class RandomNicknameGenerator {

  private static final String ADJECTIVES_PATH = "nickname/adjectives.txt";
  private static final String NOUNS_PATH = "nickname/nouns.txt";

  private static final int NUMBER_BOUND = 10_000;
  private static final int MAX_ATTEMPTS = 30;

  private final MemberRepository memberRepository;
  private final List<String> adjectives;
  private final List<String> nouns;

  public RandomNicknameGenerator(MemberRepository memberRepository) {
    this.memberRepository = memberRepository;
    this.adjectives = loadWords(ADJECTIVES_PATH);
    this.nouns = loadWords(NOUNS_PATH);
  }

  public String generateUniqueNickname() {
    for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
      String nickname = generateNickname();

      if (!memberRepository.existsByNickname(nickname)) {
        return nickname;
      }
    }

    throw new CustomException(ErrorCode.NICKNAME_GENERATION_FAILED);
  }

  private String generateNickname() {
    String adjective = getRandomWord(adjectives);
    String noun = getRandomWord(nouns);
    int number = ThreadLocalRandom.current().nextInt(NUMBER_BOUND);

    return adjective + noun + "%04d".formatted(number);
  }

  private String getRandomWord(List<String> words) {
    int index = ThreadLocalRandom.current().nextInt(words.size());
    return words.get(index);
  }

  private List<String> loadWords(String path) {
    ClassPathResource resource = new ClassPathResource(path);

    try (
      BufferedReader reader = new BufferedReader(
        new InputStreamReader(
          resource.getInputStream(),
          StandardCharsets.UTF_8
        )
      )
    ) {
      List<String> words = reader.lines()
        .map(String::trim)
        .filter(word -> !word.isBlank())
        .filter(word -> !word.startsWith("#"))
        .toList();

      if (words.isEmpty()) {
        throw new IllegalStateException("닉네임 단어 파일이 비어 있습니다: " + path);
      }

      return words;
    } catch (IOException e) {
      throw new IllegalStateException(
        "닉네임 단어 파일을 읽을 수 없습니다: " + path,
        e
      );
    }
  }
}
