package com.chuseok22.eodaegoserver.domain.admin.controller.web;

import com.chuseok22.eodaegoserver.domain.admin.dto.request.AmusementRideCreateRequest;
import com.chuseok22.eodaegoserver.domain.admin.dto.request.AmusementRideUpdateRequest;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.AmusementRideView;
import com.chuseok22.eodaegoserver.domain.admin.service.AdminAmusementRideService;
import jakarta.validation.Valid;
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
public class AdminAmusementRideController {

  private final AdminAmusementRideService adminAmusementRideService;

  @GetMapping("/admin/facility/amusement-rides")
  public String list(Model model) {
    model.addAttribute("rides", adminAmusementRideService.findAll());
    return "admin/amusement-rides/list";
  }

  @GetMapping("/admin/facility/amusement-rides/new")
  public String newForm(Model model) {
    model.addAttribute("mode", "create");
    model.addAttribute("request", new AmusementRideCreateRequest("", null, null, true));
    return "admin/amusement-rides/form";
  }

  @PostMapping("/admin/facility/amusement-rides")
  public String create(
      @Valid @ModelAttribute("request") AmusementRideCreateRequest request,
      BindingResult bindingResult,
      Model model
  ) {
    if (bindingResult.hasErrors()) {
      model.addAttribute("mode", "create");
      return "admin/amusement-rides/form";
    }
    adminAmusementRideService.create(request);
    return "redirect:/admin/facility/amusement-rides";
  }

  @GetMapping("/admin/facility/amusement-rides/{rideId}/edit")
  public String editForm(@PathVariable Integer rideId, Model model) {
    AmusementRideView ride = adminAmusementRideService.findById(rideId);
    model.addAttribute("mode", "edit");
    model.addAttribute("rideId", rideId);
    model.addAttribute("request",
        new AmusementRideUpdateRequest(ride.name(), ride.description(), ride.location(), ride.active()));
    return "admin/amusement-rides/form";
  }

  @PostMapping("/admin/facility/amusement-rides/{rideId}")
  public String update(
      @PathVariable Integer rideId,
      @Valid @ModelAttribute("request") AmusementRideUpdateRequest request,
      BindingResult bindingResult,
      Model model
  ) {
    if (bindingResult.hasErrors()) {
      model.addAttribute("mode", "edit");
      model.addAttribute("rideId", rideId);
      return "admin/amusement-rides/form";
    }
    adminAmusementRideService.update(rideId, request);
    return "redirect:/admin/facility/amusement-rides";
  }

  @PostMapping("/admin/facility/amusement-rides/{rideId}/delete")
  public String delete(@PathVariable Integer rideId) {
    adminAmusementRideService.delete(rideId);
    return "redirect:/admin/facility/amusement-rides";
  }
}
