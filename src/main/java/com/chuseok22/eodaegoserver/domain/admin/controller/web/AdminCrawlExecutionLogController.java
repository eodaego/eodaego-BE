package com.chuseok22.eodaegoserver.domain.admin.controller.web;

import com.chuseok22.eodaegoserver.domain.admin.service.CrawlExecutionLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AdminCrawlExecutionLogController {

  private final CrawlExecutionLogService crawlExecutionLogService;

  @GetMapping("/admin/crawling/history")
  public String list(@RequestParam(defaultValue = "0") int page, Model model) {
    model.addAttribute("logPage", crawlExecutionLogService.listLogs(page));
    return "admin/crawling-history/list";
  }
}
