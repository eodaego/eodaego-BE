package com.chuseok22.eodaegoserver.domain.admin.controller.web;

import com.chuseok22.eodaegoserver.domain.admin.service.AdminAiCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AdminAiCatalogController {

  private final AdminAiCatalogService adminAiCatalogService;

  @GetMapping("/admin/facility/operating-hours")
  public String operatingHoursList(Model model) {
    model.addAttribute("operatingHoursSections", adminAiCatalogService.listOperatingHours());
    return "admin/operating-hours/list";
  }

  @GetMapping("/admin/congestion")
  public String congestionList(Model model) {
    model.addAttribute("congestionSnapshots", adminAiCatalogService.listCongestion());
    return "admin/congestion/list";
  }

  @GetMapping("/admin/weather")
  public String weatherList(Model model) {
    model.addAttribute("weatherSnapshots", adminAiCatalogService.listWeather());
    return "admin/weather/list";
  }

  @PostMapping("/admin/catalog/crawl")
  public String triggerCatalogCrawl(RedirectAttributes redirectAttributes) {
    redirectAttributes.addFlashAttribute("catalogCrawlResult", adminAiCatalogService.triggerCatalogCrawl());
    return "redirect:/admin/catalog/items";
  }

  @PostMapping("/admin/facility/operating-hours/crawl")
  public String triggerOperatingHoursCrawl(RedirectAttributes redirectAttributes) {
    redirectAttributes.addFlashAttribute("crawlResult", adminAiCatalogService.triggerOperatingHoursCrawl());
    return "redirect:/admin/facility/operating-hours";
  }

  @PostMapping("/admin/weather/crawl")
  public String triggerWeatherCrawl(RedirectAttributes redirectAttributes) {
    redirectAttributes.addFlashAttribute("crawlResult", adminAiCatalogService.triggerWeatherCrawl());
    return "redirect:/admin/weather";
  }

  @PostMapping("/admin/congestion/crawl")
  public String triggerCongestionCrawl(RedirectAttributes redirectAttributes) {
    redirectAttributes.addFlashAttribute("crawlResult", adminAiCatalogService.triggerCongestionCrawl());
    return "redirect:/admin/congestion";
  }
}
