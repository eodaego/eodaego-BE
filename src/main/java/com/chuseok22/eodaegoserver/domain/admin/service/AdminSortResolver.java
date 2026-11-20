package com.chuseok22.eodaegoserver.domain.admin.service;

import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;

public final class AdminSortResolver {

  private static final String DESCENDING = "desc";

  private AdminSortResolver() {
  }

  // 허용 목록에 없는 sort 값은 요청 입력이므로 기본 정렬로 되돌려 임의 필드 정렬을 막는다.
  public static <T> Comparator<T> resolve(
      Map<String, Comparator<T>> allowedComparators,
      String sort,
      String direction,
      Comparator<T> defaultComparator
  ) {
    Comparator<T> comparator = sort == null ? null : allowedComparators.get(sort);
    if (comparator == null) {
      return defaultComparator;
    }
    return DESCENDING.equalsIgnoreCase(direction) ? comparator.reversed() : comparator;
  }

  // 이름이 비어 있는 행이 있어도 정렬 중 NPE가 나지 않도록 null을 오름차순에서 뒤로 보낸다(내림차순은 비교기를 뒤집으므로 null이 앞에 온다).
  public static <T> Comparator<T> caseInsensitive(Function<T, String> fieldExtractor) {
    return Comparator.comparing(fieldExtractor, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
  }

  public static <T, V extends Comparable<? super V>> Comparator<T> nullsLast(Function<T, V> fieldExtractor) {
    return Comparator.comparing(fieldExtractor, Comparator.nullsLast(Comparator.naturalOrder()));
  }
}
