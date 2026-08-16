package com.chuseok22.eodaegoserver.domain.admin.controller.web;

import com.chuseok22.eodaegoserver.domain.admin.service.AdminCourseRecommendationFailureLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AdminCourseRecommendationFailureLogController {

  private final AdminCourseRecommendationFailureLogService adminCourseRecommendationFailureLogService;

  @GetMapping("/admin/recommendation/failures")
  public String list(@RequestParam(defaultValue = "0") int page, Model model) {
    model.addAttribute("logPage", adminCourseRecommendationFailureLogService.listLogs(page));
    return "admin/recommendation-failures/list";
  }
}
