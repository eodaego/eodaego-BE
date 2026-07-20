package com.chuseok22.eodaegoserver.domain.admin.controller.web;

import com.chuseok22.eodaegoserver.domain.admin.dto.request.PreferenceCategoryMappingCreateRequest;
import com.chuseok22.eodaegoserver.domain.admin.service.AdminAiCatalogService;
import com.chuseok22.eodaegoserver.domain.admin.service.AdminPreferenceMappingService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AdminPreferenceMappingController {

  private static final List<String> PREFERENCE_TAGS = List.of(
      "ANIMAL", "NATURE", "ACTIVITY", "PHOTO_SPOT", "RELAXATION", "CULTURE_EVENT", "LEARNING");

  private final AdminPreferenceMappingService adminPreferenceMappingService;
  private final AdminAiCatalogService adminAiCatalogService;

  @GetMapping("/admin/recommendation/preference-mappings")
  public String list(@RequestParam(required = false) String preferenceTag, Model model) {
    model.addAttribute("mappings", adminPreferenceMappingService.findAll(preferenceTag));
    model.addAttribute("preferenceTags", PREFERENCE_TAGS);
    model.addAttribute("selectedTag", preferenceTag);
    return "admin/preference-mappings/list";
  }

  @GetMapping("/admin/recommendation/preference-mappings/new")
  public String newForm(Model model) {
    model.addAttribute("request", new PreferenceCategoryMappingCreateRequest("", ""));
    model.addAttribute("preferenceTags", PREFERENCE_TAGS);
    model.addAttribute("categories", adminAiCatalogService.listFacilityCategories());
    return "admin/preference-mappings/form";
  }

  @PostMapping("/admin/recommendation/preference-mappings")
  public String create(
      @Valid @ModelAttribute("request") PreferenceCategoryMappingCreateRequest request,
      BindingResult bindingResult,
      Model model
  ) {
    if (bindingResult.hasErrors()) {
      model.addAttribute("preferenceTags", PREFERENCE_TAGS);
      model.addAttribute("categories", adminAiCatalogService.listFacilityCategories());
      return "admin/preference-mappings/form";
    }
    adminPreferenceMappingService.create(request);
    return "redirect:/admin/recommendation/preference-mappings";
  }

  @PostMapping("/admin/recommendation/preference-mappings/{mappingId}/delete")
  public String delete(@PathVariable Integer mappingId) {
    adminPreferenceMappingService.delete(mappingId);
    return "redirect:/admin/recommendation/preference-mappings";
  }
}
