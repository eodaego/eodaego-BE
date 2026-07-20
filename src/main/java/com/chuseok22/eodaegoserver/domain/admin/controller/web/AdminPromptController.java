package com.chuseok22.eodaegoserver.domain.admin.controller.web;

import com.chuseok22.eodaegoserver.domain.admin.dto.request.PromptTemplateCreateRequest;
import com.chuseok22.eodaegoserver.domain.admin.dto.request.PromptTemplateUpdateRequest;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.PromptTemplateView;
import com.chuseok22.eodaegoserver.domain.admin.service.AdminPromptService;
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

@Controller
@RequiredArgsConstructor
public class AdminPromptController {

  private static final List<String> PURPOSES = List.of("chat", "recommendation");

  private final AdminPromptService adminPromptService;

  @GetMapping("/admin/prompts")
  public String list(Model model) {
    model.addAttribute("prompts", adminPromptService.findAll());
    return "admin/prompts/list";
  }

  @GetMapping("/admin/prompts/new")
  public String newForm(Model model) {
    model.addAttribute("mode", "create");
    model.addAttribute("request", new PromptTemplateCreateRequest("", "", "recommendation", "", true));
    model.addAttribute("models", adminPromptService.listModels());
    model.addAttribute("purposes", PURPOSES);
    return "admin/prompts/form";
  }

  @PostMapping("/admin/prompts")
  public String create(
      @Valid @ModelAttribute("request") PromptTemplateCreateRequest request,
      BindingResult bindingResult,
      Model model
  ) {
    if (bindingResult.hasErrors()) {
      model.addAttribute("mode", "create");
      model.addAttribute("models", adminPromptService.listModels());
      model.addAttribute("purposes", PURPOSES);
      return "admin/prompts/form";
    }
    adminPromptService.create(request);
    return "redirect:/admin/prompts";
  }

  @GetMapping("/admin/prompts/{promptId}/edit")
  public String editForm(@PathVariable Integer promptId, Model model) {
    PromptTemplateView prompt = adminPromptService.findById(promptId);
    model.addAttribute("mode", "edit");
    model.addAttribute("promptId", promptId);
    model.addAttribute("request", new PromptTemplateUpdateRequest(
        prompt.name(), prompt.model(), prompt.purpose(), prompt.templateText(), prompt.active()));
    model.addAttribute("models", adminPromptService.listModels());
    model.addAttribute("purposes", PURPOSES);
    return "admin/prompts/form";
  }

  @PostMapping("/admin/prompts/{promptId}")
  public String update(
      @PathVariable Integer promptId,
      @Valid @ModelAttribute("request") PromptTemplateUpdateRequest request,
      BindingResult bindingResult,
      Model model
  ) {
    if (bindingResult.hasErrors()) {
      model.addAttribute("mode", "edit");
      model.addAttribute("promptId", promptId);
      model.addAttribute("models", adminPromptService.listModels());
      model.addAttribute("purposes", PURPOSES);
      return "admin/prompts/form";
    }
    adminPromptService.update(promptId, request);
    return "redirect:/admin/prompts";
  }

  @PostMapping("/admin/prompts/{promptId}/delete")
  public String delete(@PathVariable Integer promptId) {
    adminPromptService.delete(promptId);
    return "redirect:/admin/prompts";
  }

  @PostMapping("/admin/prompts/{promptId}/activate")
  public String activate(@PathVariable Integer promptId) {
    adminPromptService.activate(promptId);
    return "redirect:/admin/prompts";
  }
}
