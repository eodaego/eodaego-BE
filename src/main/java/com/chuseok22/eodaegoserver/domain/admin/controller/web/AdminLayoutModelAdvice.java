package com.chuseok22.eodaegoserver.domain.admin.controller.web;

import com.chuseok22.eodaegoserver.domain.admin.service.AdminDashboardService;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.util.UriComponentsBuilder;

@ControllerAdvice(basePackages = "com.chuseok22.eodaegoserver.domain.admin.controller.web")
@RequiredArgsConstructor
public class AdminLayoutModelAdvice {

  private static final String PAGE_PARAMETER_NAME = "page";
  private static final String PAGE_PLACEHOLDER = "{page}";
  private static final String SORT_PARAMETER_NAME = "sort";
  private static final String DIRECTION_PARAMETER_NAME = "direction";

  private final AdminDashboardService adminDashboardService;

  // 로그인 페이지처럼 인증 전 요청에서는 사이드바를 그리지 않으므로 DB 조회를 건너뛴다.
  @ModelAttribute("recommendationFailureBadge")
  public long recommendationFailureBadge(Principal principal) {
    if (principal == null) {
      return 0L;
    }
    return adminDashboardService.countRecentRecommendationFailures();
  }

  @ModelAttribute("adminAccountName")
  public String adminAccountName(Principal principal) {
    return principal == null ? null : principal.getName();
  }

  // 정렬·필터 쿼리를 보존한 채 페이지 번호만 바꾸는 링크를 템플릿이 만들 수 있게 page 자리를 비워 둔다.
  @ModelAttribute("pageUrlTemplate")
  public String pageUrlTemplate(HttpServletRequest request) {
    return UriComponentsBuilder.newInstance()
        .path(request.getRequestURI())
        .query(request.getQueryString())
        .replaceQueryParam(PAGE_PARAMETER_NAME, PAGE_PLACEHOLDER)
        .build()
        .toUriString();
  }

  // 정렬을 바꾸면 결과 순서가 달라지므로 page를 지워 첫 페이지로 돌아가고, 나머지 필터는 그대로 둔다.
  @ModelAttribute("sortUrlTemplate")
  public String sortUrlTemplate(HttpServletRequest request) {
    return UriComponentsBuilder.newInstance()
        .path(request.getRequestURI())
        .query(request.getQueryString())
        .replaceQueryParam(PAGE_PARAMETER_NAME)
        .replaceQueryParam(SORT_PARAMETER_NAME, "{sort}")
        .replaceQueryParam(DIRECTION_PARAMETER_NAME, "{direction}")
        .build()
        .toUriString();
  }
}
