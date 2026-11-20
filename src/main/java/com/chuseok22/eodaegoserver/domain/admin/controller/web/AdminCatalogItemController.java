package com.chuseok22.eodaegoserver.domain.admin.controller.web;

import com.chuseok22.eodaegoserver.domain.admin.dto.request.CatalogItemListQuery;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.AdminActionResult;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.CatalogItemEditView;
import com.chuseok22.eodaegoserver.domain.admin.service.AdminCatalogItemService;
import com.chuseok22.eodaegoserver.domain.catalog.CatalogItemStatus;
import com.chuseok22.eodaegoserver.domain.catalog.dto.request.CatalogItemUpdateRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
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
  public String list(
      @RequestParam(required = false) String category,
      @RequestParam(defaultValue = "false") boolean onlyMissingChildDescription,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String sort,
      @RequestParam(required = false) String direction,
      @RequestParam(defaultValue = "0") int page,
      HttpServletRequest request,
      Model model
  ) {
    CatalogItemListQuery query =
        new CatalogItemListQuery(category, onlyMissingChildDescription, keyword, sort, direction, page);
    model.addAttribute("query", query);
    model.addAttribute("page", adminCatalogItemService.listItems(query));
    return HtmxRequestSupport.isPartialRequest(request)
        ? "admin/catalog-item/list :: listRegion"
        : "admin/catalog-item/list";
  }

  @GetMapping("/admin/catalog/items/export")
  public ResponseEntity<byte[]> export(
      @RequestParam(required = false) String category,
      @RequestParam(defaultValue = "false") boolean onlyMissingChildDescription,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String sort,
      @RequestParam(required = false) String direction
  ) {
    CatalogItemListQuery query =
        new CatalogItemListQuery(category, onlyMissingChildDescription, keyword, sort, direction, 0);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"catalog-items.csv\"")
        .body(adminCatalogItemService.exportItemsCsv(query));
  }

  @PostMapping("/admin/catalog/items/bulk-status")
  public String bulkChangeStatus(
      @RequestParam(required = false) List<UUID> catalogItemIds,
      @RequestParam(required = false) CatalogItemStatus status,
      RedirectAttributes redirectAttributes
  ) {
    redirectAttributes.addFlashAttribute("actionResult", adminCatalogItemService.bulkChangeStatus(catalogItemIds, status));
    return "redirect:/admin/catalog/items";
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
      Model model,
      RedirectAttributes redirectAttributes
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
    redirectAttributes.addFlashAttribute("actionResult", new AdminActionResult(true, "도감 항목을 수정했습니다."));
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
