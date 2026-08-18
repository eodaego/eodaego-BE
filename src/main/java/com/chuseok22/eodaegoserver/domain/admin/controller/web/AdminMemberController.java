package com.chuseok22.eodaegoserver.domain.admin.controller.web;

import com.chuseok22.eodaegoserver.domain.admin.service.AdminMemberService;
import com.chuseok22.eodaegoserver.domain.member.SocialType;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AdminMemberController {

  private final AdminMemberService adminMemberService;

  @GetMapping("/admin/members")
  public String list(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) SocialType socialType,
      @RequestParam(defaultValue = "0") int page,
      Model model
  ) {
    model.addAttribute("memberPage", adminMemberService.searchMembers(keyword, socialType, page));
    model.addAttribute("keyword", keyword);
    model.addAttribute("socialType", socialType);
    return "admin/member/list";
  }

  @GetMapping("/admin/members/{memberId}")
  public String detail(@PathVariable UUID memberId, Model model) {
    model.addAttribute("member", adminMemberService.getMemberDetail(memberId));
    return "admin/member/detail";
  }
}
