package com.chuseok22.eodaegoserver.domain.admin.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminDashboardController {

  @GetMapping("/admin/dashboard")
  public String dashboard() {
    return "admin/dashboard";
  }
}
