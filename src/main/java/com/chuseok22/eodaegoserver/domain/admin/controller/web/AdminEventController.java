package com.chuseok22.eodaegoserver.domain.admin.controller.web;

import com.chuseok22.eodaegoserver.domain.admin.service.AdminEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AdminEventController {

  private final AdminEventService adminEventService;

  @GetMapping("/admin/events")
  public String list(Model model) {
    model.addAttribute("events", adminEventService.listEvents());
    return "admin/events/list";
  }

  @PostMapping("/admin/events/crawl")
  public String triggerCrawl(RedirectAttributes redirectAttributes) {
    redirectAttributes.addFlashAttribute("crawlResult", adminEventService.triggerEventCrawl());
    return "redirect:/admin/events";
  }
}
