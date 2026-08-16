package com.chuseok22.eodaegoserver.domain.admin.controller.web;

import com.chuseok22.eodaegoserver.domain.admin.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AdminDashboardController {

  private final AdminDashboardService adminDashboardService;

  @GetMapping("/admin/dashboard")
  public String dashboard(Model model) {
    model.addAttribute("aiHealth", adminDashboardService.getAiHealthStatus());
    return "admin/dashboard";
  }
}
