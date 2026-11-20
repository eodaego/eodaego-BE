package com.chuseok22.eodaegoserver.domain.admin.controller.web;

import com.chuseok22.eodaegoserver.domain.admin.service.AdminCrawlRetryService;
import com.chuseok22.eodaegoserver.domain.admin.service.CrawlExecutionLogService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AdminCrawlExecutionLogController {

  private static final String HISTORY_RETURN_TARGET = "history";
  // returnTo 값을 그대로 리다이렉트에 쓰지 않고 고정 경로로만 매핑해 오픈 리다이렉트를 막는다.
  private static final Map<String, String> RETURN_PATHS = Map.of(
      "dashboard", "/admin/dashboard",
      HISTORY_RETURN_TARGET, "/admin/crawling/history"
  );

  private final CrawlExecutionLogService crawlExecutionLogService;
  private final AdminCrawlRetryService adminCrawlRetryService;

  @GetMapping("/admin/crawling/history")
  public String list(@RequestParam(defaultValue = "0") int page, Model model) {
    model.addAttribute("logPage", crawlExecutionLogService.listLogs(page));
    model.addAttribute("retryableLogIds", crawlExecutionLogService.retryableLogIds());
    return "admin/crawling-history/list";
  }

  @PostMapping("/admin/crawling/retry/{jobType}")
  public String retry(
      @PathVariable String jobType,
      @RequestParam(defaultValue = HISTORY_RETURN_TARGET) String returnTo,
      RedirectAttributes redirectAttributes
  ) {
    redirectAttributes.addFlashAttribute("actionResult", adminCrawlRetryService.retry(jobType));
    return "redirect:" + RETURN_PATHS.getOrDefault(returnTo, RETURN_PATHS.get(HISTORY_RETURN_TARGET));
  }
}
