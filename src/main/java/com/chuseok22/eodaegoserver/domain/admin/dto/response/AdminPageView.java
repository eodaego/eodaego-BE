package com.chuseok22.eodaegoserver.domain.admin.dto.response;

import java.util.List;

public record AdminPageView<T>(List<T> content, int number, int size, long totalElements, int totalPages) {

  // 범위를 벗어난 페이지 요청(?page=999, ?page=-1)이 빈 화면이나 예외가 되지 않도록 유효 범위로 보정한다.
  public static <T> AdminPageView<T> of(List<T> allItems, int requestedPage, int pageSize) {
    int totalPages = Math.max(1, (int) Math.ceil((double) allItems.size() / pageSize));
    int number = Math.min(Math.max(requestedPage, 0), totalPages - 1);
    int fromIndex = Math.min(number * pageSize, allItems.size());
    int toIndex = Math.min(fromIndex + pageSize, allItems.size());
    return new AdminPageView<>(List.copyOf(allItems.subList(fromIndex, toIndex)), number, pageSize, allItems.size(), totalPages);
  }

  public boolean hasPrevious() {
    return number > 0;
  }

  public boolean hasNext() {
    return number < totalPages - 1;
  }
}
