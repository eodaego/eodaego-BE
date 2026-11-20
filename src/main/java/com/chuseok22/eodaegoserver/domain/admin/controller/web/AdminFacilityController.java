package com.chuseok22.eodaegoserver.domain.admin.controller.web;

import com.chuseok22.eodaegoserver.domain.admin.dto.request.FacilityCreateRequest;
import com.chuseok22.eodaegoserver.domain.admin.dto.request.FacilityUpdateRequest;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.AdminActionResult;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.FacilityView;
import com.chuseok22.eodaegoserver.domain.admin.service.AdminFacilityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
public class AdminFacilityController {

  private final AdminFacilityService adminFacilityService;

  @GetMapping("/admin/facility")
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
    model.addAttribute("page", adminFacilityService.searchFacilities(keyword, sort, direction, page));
    return HtmxRequestSupport.isPartialRequest(request)
        ? "admin/facility/list :: listRegion"
        : "admin/facility/list";
  }

  @GetMapping("/admin/facility/new")
  public String newForm(Model model) {
    model.addAttribute("mode", "create");
    model.addAttribute("request", new FacilityCreateRequest("", "", null, null, null, null, null, null));
    return "admin/facility/form";
  }

  @PostMapping("/admin/facility")
  public String create(
      @Valid @ModelAttribute("request") FacilityCreateRequest request,
      BindingResult bindingResult,
      Model model,
      RedirectAttributes redirectAttributes
  ) {
    if (bindingResult.hasErrors()) {
      model.addAttribute("mode", "create");
      return "admin/facility/form";
    }
    adminFacilityService.create(request);
    redirectAttributes.addFlashAttribute("actionResult", new AdminActionResult(true, "시설을 등록했습니다."));
    return "redirect:/admin/facility";
  }

  @GetMapping("/admin/facility/{facilityId}/edit")
  public String editForm(@PathVariable Integer facilityId, Model model) {
    FacilityView facility = adminFacilityService.findById(facilityId);
    model.addAttribute("mode", "edit");
    model.addAttribute("facilityId", facilityId);
    model.addAttribute("request", new FacilityUpdateRequest(
        facility.category(), facility.name(), facility.code(), facility.intro(),
        facility.description(), facility.latitude(), facility.longitude(), facility.facilityType()));
    return "admin/facility/form";
  }

  @PostMapping("/admin/facility/{facilityId}")
  public String update(
      @PathVariable Integer facilityId,
      @Valid @ModelAttribute("request") FacilityUpdateRequest request,
      BindingResult bindingResult,
      Model model,
      RedirectAttributes redirectAttributes
  ) {
    if (bindingResult.hasErrors()) {
      model.addAttribute("mode", "edit");
      model.addAttribute("facilityId", facilityId);
      return "admin/facility/form";
    }
    adminFacilityService.update(facilityId, request);
    redirectAttributes.addFlashAttribute("actionResult", new AdminActionResult(true, "시설을 수정했습니다."));
    return "redirect:/admin/facility";
  }

  @PostMapping("/admin/facility/{facilityId}/delete")
  public String delete(@PathVariable Integer facilityId, RedirectAttributes redirectAttributes) {
    adminFacilityService.delete(facilityId);
    redirectAttributes.addFlashAttribute("actionResult", new AdminActionResult(true, "시설을 삭제했습니다."));
    return "redirect:/admin/facility";
  }

  @PostMapping("/admin/facility/import")
  public String triggerFacilityImport(RedirectAttributes redirectAttributes) {
    redirectAttributes.addFlashAttribute("crawlResult", adminFacilityService.triggerFacilityImport());
    return "redirect:/admin/facility";
  }
}
