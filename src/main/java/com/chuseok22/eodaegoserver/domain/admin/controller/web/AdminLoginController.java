package com.chuseok22.eodaegoserver.domain.admin.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminLoginController {

  @GetMapping("/admin/login")
  public String loginPage() {
    return "admin/login";
  }
}
