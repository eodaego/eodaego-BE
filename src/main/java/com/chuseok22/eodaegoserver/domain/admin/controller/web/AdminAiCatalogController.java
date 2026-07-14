package com.chuseok22.eodaegoserver.domain.admin.controller.web;

import com.chuseok22.eodaegoserver.domain.admin.service.AdminAiCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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
}
