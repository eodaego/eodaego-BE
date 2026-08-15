package com.chuseok22.eodaegoserver.domain.admin.controller.web;

import com.chuseok22.eodaegoserver.domain.admin.dto.response.CatalogItemEditView;
import com.chuseok22.eodaegoserver.domain.admin.service.AdminCatalogItemService;
import com.chuseok22.eodaegoserver.domain.catalog.dto.request.CatalogItemUpdateRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AdminCatalogItemController {

  private final AdminCatalogItemService adminCatalogItemService;

  @GetMapping("/admin/catalog/items")
  public String list(@RequestParam(required = false) String category, Model model) {
    model.addAttribute("items", adminCatalogItemService.listItems(category));
    model.addAttribute("selectedCategory", category);
    return "admin/catalog-item/list";
  }

  @GetMapping("/admin/catalog/items/{catalogItemId}/edit")
  public String editForm(@PathVariable UUID catalogItemId, Model model) {
    addEditViewAttributes(catalogItemId, model);
    return "admin/catalog-item/form";
  }

  @PostMapping("/admin/catalog/items/{catalogItemId}")
  public String update(
      @PathVariable UUID catalogItemId,
      @Valid @ModelAttribute("request") CatalogItemUpdateRequest request,
      BindingResult bindingResult,
      Model model
  ) {
    if (bindingResult.hasErrors()) {
      model.addAttribute("catalogItemId", catalogItemId);
      CatalogItemEditView editView = adminCatalogItemService.getEditView(catalogItemId);
      model.addAttribute("item", editView.item());
      model.addAttribute("originalName", editView.originalName());
      model.addAttribute("originalFeature", editView.originalFeature());
      model.addAttribute("originalImageUrl", editView.originalImageUrl());
      model.addAttribute("originalLatitude", editView.originalLatitude());
      model.addAttribute("originalLongitude", editView.originalLongitude());
      return "admin/catalog-item/form";
    }
    adminCatalogItemService.updateItem(catalogItemId, request);
    return "redirect:/admin/catalog/items";
  }

  @PostMapping("/admin/catalog/items/sync")
  public String sync(RedirectAttributes redirectAttributes) {
    redirectAttributes.addFlashAttribute("crawlResult", adminCatalogItemService.triggerSync());
    return "redirect:/admin/catalog/items";
  }

  private void addEditViewAttributes(UUID catalogItemId, Model model) {
    CatalogItemEditView editView = adminCatalogItemService.getEditView(catalogItemId);
    model.addAttribute("catalogItemId", catalogItemId);
    model.addAttribute("item", editView.item());
    model.addAttribute("originalName", editView.originalName());
    model.addAttribute("originalFeature", editView.originalFeature());
    model.addAttribute("originalImageUrl", editView.originalImageUrl());
    model.addAttribute("originalLatitude", editView.originalLatitude());
    model.addAttribute("originalLongitude", editView.originalLongitude());
    model.addAttribute("request", editView.request());
  }
}
