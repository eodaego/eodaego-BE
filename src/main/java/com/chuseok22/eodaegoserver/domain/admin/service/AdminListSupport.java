package com.chuseok22.eodaegoserver.domain.admin.service;

import com.chuseok22.eodaegoserver.domain.admin.dto.response.AdminPageView;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

// AI 서버가 내려준 전체 목록에 검색·정렬·페이징을 적용하는 공통 처리. 서비스는 키워드가 일치할 필드와 정렬 허용 목록만 정한다.
public final class AdminListSupport {

  public static final int PAGE_SIZE = 20;

  private AdminListSupport() {
  }

  // 정렬 값이 없거나 허용 목록 밖이면 AI 서버 순서를 유지한다(정렬이 안정적이므로 비교 결과 0이면 순서가 보존된다).
  public static <T> AdminPageView<T> paginate(
      List<T> items,
      Predicate<T> filter,
      Map<String, Comparator<T>> allowedComparators,
      String sort,
      String direction,
      int page
  ) {
    List<T> processedItems = items.stream()
        .filter(filter)
        .sorted(AdminSortResolver.resolve(allowedComparators, sort, direction, (first, second) -> 0))
        .toList();
    return AdminPageView.of(processedItems, page, PAGE_SIZE);
  }

  // 키워드가 비어 있으면 모든 행을 통과시키고, 아니면 지정한 필드 중 하나라도 대소문자 무시로 포함하면 통과시킨다.
  @SafeVarargs
  public static <T> Predicate<T> keywordFilter(String keyword, Function<T, String>... fieldExtractors) {
    String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
    if (normalizedKeyword.isEmpty()) {
      return item -> true;
    }
    return item -> Arrays.stream(fieldExtractors)
        .map(fieldExtractor -> fieldExtractor.apply(item))
        .anyMatch(field -> field != null && field.toLowerCase(Locale.ROOT).contains(normalizedKeyword));
  }
}
