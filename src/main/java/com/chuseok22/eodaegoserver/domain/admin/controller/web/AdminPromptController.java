package com.chuseok22.eodaegoserver.domain.admin.controller.web;

import com.chuseok22.eodaegoserver.domain.admin.dto.request.PromptTemplateCreateRequest;
import com.chuseok22.eodaegoserver.domain.admin.dto.request.PromptTemplateProviderCreateRequest;
import com.chuseok22.eodaegoserver.domain.admin.dto.request.PromptTemplateProviderUpdateRequest;
import com.chuseok22.eodaegoserver.domain.admin.dto.request.PromptTemplateUpdateRequest;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.AdminActionResult;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.PromptTemplateView;
import com.chuseok22.eodaegoserver.domain.admin.service.AdminPromptService;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AdminPromptController {

  private static final List<String> PURPOSES = List.of("chat", "recommendation", "photo_recognition");

  private final AdminPromptService adminPromptService;

  @GetMapping("/admin/prompts")
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
    model.addAttribute("page", adminPromptService.searchPrompts(keyword, sort, direction, page));
    return HtmxRequestSupport.isPartialRequest(request)
        ? "admin/prompts/list :: listRegion"
        : "admin/prompts/list";
  }

  @GetMapping("/admin/prompts/new")
  public String newForm(Model model) {
    model.addAttribute("mode", "create");
    model.addAttribute("request", new PromptTemplateCreateRequest("", "recommendation", "", false));
    model.addAttribute("purposes", PURPOSES);
    return "admin/prompts/form";
  }

  @PostMapping("/admin/prompts")
  public String create(
      @Valid @ModelAttribute("request") PromptTemplateCreateRequest request,
      BindingResult bindingResult,
      Model model,
      RedirectAttributes redirectAttributes
  ) {
    if (bindingResult.hasErrors()) {
      model.addAttribute("mode", "create");
      model.addAttribute("purposes", PURPOSES);
      return "admin/prompts/form";
    }
    PromptTemplateView created = adminPromptService.create(request);
    redirectAttributes.addFlashAttribute("actionResult", new AdminActionResult(true, "프롬프트 템플릿을 등록했습니다. 비활성 상태로 생성되었습니다."));
    return "redirect:/admin/prompts/" + created.id() + "/edit";
  }

  @GetMapping("/admin/prompts/{promptId}/edit")
  public String editForm(@PathVariable Integer promptId, Model model) {
    PromptTemplateView prompt = adminPromptService.findById(promptId);
    model.addAttribute("mode", "edit");
    model.addAttribute("promptId", promptId);
    model.addAttribute("request", new PromptTemplateUpdateRequest(
        prompt.name(), prompt.purpose(), prompt.templateText(), prompt.active()));
    model.addAttribute("purposes", PURPOSES);
    model.addAttribute("providers", prompt.providers());
    model.addAttribute("newProviderRequest", new PromptTemplateProviderCreateRequest("SUH_AIDER", "", 1, true));
    return "admin/prompts/form";
  }

  @PostMapping("/admin/prompts/{promptId}")
  public String update(
      @PathVariable Integer promptId,
      @Valid @ModelAttribute("request") PromptTemplateUpdateRequest request,
      BindingResult bindingResult,
      Model model,
      RedirectAttributes redirectAttributes
  ) {
    if (bindingResult.hasErrors()) {
      model.addAttribute("mode", "edit");
      model.addAttribute("promptId", promptId);
      model.addAttribute("purposes", PURPOSES);
      PromptTemplateView prompt = adminPromptService.findById(promptId);
      model.addAttribute("providers", prompt.providers());
      model.addAttribute("newProviderRequest", new PromptTemplateProviderCreateRequest("SUH_AIDER", "", 1, true));
      return "admin/prompts/form";
    }
    adminPromptService.update(promptId, request);
    redirectAttributes.addFlashAttribute("actionResult", new AdminActionResult(true, "프롬프트 템플릿을 수정했습니다."));
    return "redirect:/admin/prompts";
  }

  @PostMapping("/admin/prompts/{promptId}/delete")
  public String delete(@PathVariable Integer promptId, RedirectAttributes redirectAttributes) {
    adminPromptService.delete(promptId);
    redirectAttributes.addFlashAttribute("actionResult", new AdminActionResult(true, "프롬프트 템플릿을 삭제했습니다."));
    return "redirect:/admin/prompts";
  }

  @PostMapping("/admin/prompts/{promptId}/activate")
  public String activate(@PathVariable Integer promptId, RedirectAttributes redirectAttributes) {
    adminPromptService.activate(promptId);
    redirectAttributes.addFlashAttribute("actionResult", new AdminActionResult(true, "프롬프트 템플릿을 활성화했습니다."));
    return "redirect:/admin/prompts";
  }

  @PostMapping("/admin/prompts/{promptId}/providers")
  public String createProvider(
      @PathVariable Integer promptId,
      @Valid @ModelAttribute("newProviderRequest") PromptTemplateProviderCreateRequest request,
      BindingResult bindingResult,
      Model model,
      RedirectAttributes redirectAttributes
  ) {
    if (bindingResult.hasErrors()) {
      PromptTemplateView prompt = adminPromptService.findById(promptId);
      model.addAttribute("mode", "edit");
      model.addAttribute("promptId", promptId);
      model.addAttribute("purposes", PURPOSES);
      model.addAttribute("request", new PromptTemplateUpdateRequest(
          prompt.name(), prompt.purpose(), prompt.templateText(), prompt.active()));
      model.addAttribute("providers", prompt.providers());
      return "admin/prompts/form";
    }
    adminPromptService.createProvider(promptId, request);
    redirectAttributes.addFlashAttribute("actionResult", new AdminActionResult(true, "Provider를 추가했습니다."));
    return "redirect:/admin/prompts/" + promptId + "/edit";
  }

  @PostMapping("/admin/prompts/{promptId}/providers/{providerId}")
  public String updateProvider(
      @PathVariable Integer promptId,
      @PathVariable Integer providerId,
      @ModelAttribute PromptTemplateProviderUpdateRequest request,
      RedirectAttributes redirectAttributes
  ) {
    adminPromptService.updateProvider(promptId, providerId, request);
    redirectAttributes.addFlashAttribute("actionResult", new AdminActionResult(true, "Provider를 수정했습니다."));
    return "redirect:/admin/prompts/" + promptId + "/edit";
  }

  @PostMapping("/admin/prompts/{promptId}/providers/{providerId}/delete")
  public String deleteProvider(@PathVariable Integer promptId, @PathVariable Integer providerId, RedirectAttributes redirectAttributes) {
    adminPromptService.deleteProvider(promptId, providerId);
    redirectAttributes.addFlashAttribute("actionResult", new AdminActionResult(true, "Provider를 삭제했습니다."));
    return "redirect:/admin/prompts/" + promptId + "/edit";
  }
}
