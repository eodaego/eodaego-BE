package com.chuseok22.eodaegoserver.domain.admin.controller.web;

import com.chuseok22.eodaegoserver.domain.admin.service.AdminAiCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AdminAiCatalogController {

  private final AdminAiCatalogService adminAiCatalogService;

  @GetMapping("/admin/facility")
  public String facilityList(Model model) {
    model.addAttribute("facilities", adminAiCatalogService.listFacilities());
    return "admin/facility/list";
  }

  @GetMapping("/admin/facility/operating-hours")
  public String operatingHoursList(Model model) {
    model.addAttribute("operatingHoursSections", adminAiCatalogService.listOperatingHours());
    return "admin/operating-hours/list";
  }

  @GetMapping("/admin/catalog/animals")
  public String animalList(Model model) {
    model.addAttribute("animals", adminAiCatalogService.listAnimals());
    return "admin/catalog/animals";
  }

  @GetMapping("/admin/catalog/plants")
  public String plantList(Model model) {
    model.addAttribute("plants", adminAiCatalogService.listPlants());
    return "admin/catalog/plants";
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
  public String triggerCatalogCrawl(@RequestParam String redirectTo, RedirectAttributes redirectAttributes) {
    redirectAttributes.addFlashAttribute("catalogCrawlResult", adminAiCatalogService.triggerCatalogCrawl());
    return "plants".equals(redirectTo) ? "redirect:/admin/catalog/plants" : "redirect:/admin/catalog/animals";
  }

  @PostMapping("/admin/facility/operating-hours/crawl")
  public String triggerOperatingHoursCrawl(RedirectAttributes redirectAttributes) {
    redirectAttributes.addFlashAttribute("crawlResult", adminAiCatalogService.triggerOperatingHoursCrawl());
    return "redirect:/admin/facility/operating-hours";
  }

  @PostMapping("/admin/facility/import")
  public String triggerFacilityImport(RedirectAttributes redirectAttributes) {
    redirectAttributes.addFlashAttribute("crawlResult", adminAiCatalogService.triggerFacilityImport());
    return "redirect:/admin/facility";
  }

  @PostMapping("/admin/weather/crawl")
  public String triggerWeatherCrawl(RedirectAttributes redirectAttributes) {
    redirectAttributes.addFlashAttribute("crawlResult", adminAiCatalogService.triggerWeatherCrawl());
    return "redirect:/admin/weather";
  }
}
