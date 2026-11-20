package com.chuseok22.eodaegoserver.domain.admin.controller.web;

import jakarta.servlet.http.HttpServletRequest;

public final class HtmxRequestSupport {

  private static final String HTMX_REQUEST_HEADER = "HX-Request";
  private static final String HTMX_HISTORY_RESTORE_HEADER = "HX-History-Restore-Request";

  private HtmxRequestSupport() {
  }

  // 브라우저 뒤로 가기로 히스토리를 복원할 때도 HX-Request가 오지만, 이때는 조각이 아닌 전체 페이지가 필요하다.
  public static boolean isPartialRequest(HttpServletRequest request) {
    return request.getHeader(HTMX_REQUEST_HEADER) != null
        && request.getHeader(HTMX_HISTORY_RESTORE_HEADER) == null;
  }
}
