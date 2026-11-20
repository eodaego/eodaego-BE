package com.chuseok22.eodaegoserver.domain.admin.controller.web;

import com.chuseok22.eodaegoserver.domain.admin.dto.request.CrawlingScheduleCreateRequest;
import com.chuseok22.eodaegoserver.domain.admin.dto.request.CrawlingScheduleUpdateRequest;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.AdminActionResult;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.CrawlingScheduleView;
import com.chuseok22.eodaegoserver.domain.admin.service.AdminCrawlingScheduleService;
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
public class AdminCrawlingScheduleController {

  private final AdminCrawlingScheduleService adminCrawlingScheduleService;

  @GetMapping("/admin/crawling/schedules")
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
    model.addAttribute("page", adminCrawlingScheduleService.searchSchedules(keyword, sort, direction, page));
    return HtmxRequestSupport.isPartialRequest(request)
        ? "admin/crawling-schedules/list :: listRegion"
        : "admin/crawling-schedules/list";
  }

  @GetMapping("/admin/crawling/schedules/new")
  public String newForm(Model model) {
    model.addAttribute("mode", "create");
    model.addAttribute("request", new CrawlingScheduleCreateRequest("", "interval", "", true));
    return "admin/crawling-schedules/form";
  }

  @PostMapping("/admin/crawling/schedules")
  public String create(
      @Valid @ModelAttribute("request") CrawlingScheduleCreateRequest request,
      BindingResult bindingResult,
      Model model,
      RedirectAttributes redirectAttributes
  ) {
    if (bindingResult.hasErrors()) {
      model.addAttribute("mode", "create");
      return "admin/crawling-schedules/form";
    }
    adminCrawlingScheduleService.create(request);
    redirectAttributes.addFlashAttribute("actionResult", new AdminActionResult(true, "크롤링 스케줄을 등록했습니다."));
    return "redirect:/admin/crawling/schedules";
  }

  @GetMapping("/admin/crawling/schedules/{scheduleId}/edit")
  public String editForm(@PathVariable Integer scheduleId, Model model) {
    CrawlingScheduleView schedule = adminCrawlingScheduleService.findById(scheduleId);
    model.addAttribute("mode", "edit");
    model.addAttribute("scheduleId", scheduleId);
    model.addAttribute("jobId", schedule.jobId());
    model.addAttribute("request",
        new CrawlingScheduleUpdateRequest(schedule.triggerType(), schedule.triggerConfig(), schedule.active()));
    return "admin/crawling-schedules/form";
  }

  @PostMapping("/admin/crawling/schedules/{scheduleId}")
  public String update(
      @PathVariable Integer scheduleId,
      @Valid @ModelAttribute("request") CrawlingScheduleUpdateRequest request,
      BindingResult bindingResult,
      Model model,
      RedirectAttributes redirectAttributes
  ) {
    if (bindingResult.hasErrors()) {
      model.addAttribute("mode", "edit");
      model.addAttribute("scheduleId", scheduleId);
      model.addAttribute("jobId", adminCrawlingScheduleService.findById(scheduleId).jobId());
      return "admin/crawling-schedules/form";
    }
    adminCrawlingScheduleService.update(scheduleId, request);
    redirectAttributes.addFlashAttribute("actionResult", new AdminActionResult(true, "크롤링 스케줄을 수정했습니다."));
    return "redirect:/admin/crawling/schedules";
  }

  @PostMapping("/admin/crawling/schedules/{scheduleId}/delete")
  public String delete(@PathVariable Integer scheduleId, RedirectAttributes redirectAttributes) {
    adminCrawlingScheduleService.delete(scheduleId);
    redirectAttributes.addFlashAttribute("actionResult", new AdminActionResult(true, "크롤링 스케줄을 삭제했습니다."));
    return "redirect:/admin/crawling/schedules";
  }
}
