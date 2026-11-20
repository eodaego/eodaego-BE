package com.chuseok22.eodaegoserver.domain.admin.controller.web;

import com.chuseok22.eodaegoserver.domain.admin.service.AdminEventService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AdminEventController {

  private final AdminEventService adminEventService;

  @GetMapping("/admin/events")
  public String list(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String sort,
      @RequestParam(required = false) String direction,
      @RequestParam(defaultValue = "0") int page,
      HttpServletRequest request,
      Model model
  ) {
    model.addAttribute("keyword", keyword);
    model.addAttribute("sort", sort);
    model.addAttribute("direction", direction);
    model.addAttribute("page", adminEventService.searchEvents(keyword, sort, direction, page));
    return HtmxRequestSupport.isPartialRequest(request)
        ? "admin/events/list :: listRegion"
        : "admin/events/list";
  }

  @PostMapping("/admin/events/crawl")
  public String triggerCrawl(RedirectAttributes redirectAttributes) {
    redirectAttributes.addFlashAttribute("crawlResult", adminEventService.triggerEventCrawl());
    return "redirect:/admin/events";
  }
}
