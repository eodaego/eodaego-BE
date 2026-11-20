package com.chuseok22.eodaegoserver.domain.admin.controller.web;

import com.chuseok22.eodaegoserver.domain.admin.dto.request.MemberListQuery;
import com.chuseok22.eodaegoserver.domain.admin.service.AdminMemberService;
import com.chuseok22.eodaegoserver.domain.member.DeviceType;
import com.chuseok22.eodaegoserver.domain.member.SocialType;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
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
      @RequestParam(required = false) DeviceType deviceType,
      @RequestParam(required = false) String sort,
      @RequestParam(required = false) String direction,
      @RequestParam(defaultValue = "0") int page,
      HttpServletRequest request,
      Model model
  ) {
    MemberListQuery query = new MemberListQuery(keyword, socialType, deviceType, sort, direction, page);
    model.addAttribute("query", query);
    model.addAttribute("memberPage", adminMemberService.searchMembers(query));
    return HtmxRequestSupport.isPartialRequest(request)
        ? "admin/member/list :: listRegion"
        : "admin/member/list";
  }

  @GetMapping("/admin/members/export")
  public ResponseEntity<byte[]> export(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) SocialType socialType,
      @RequestParam(required = false) DeviceType deviceType,
      @RequestParam(required = false) String sort,
      @RequestParam(required = false) String direction
  ) {
    MemberListQuery query = new MemberListQuery(keyword, socialType, deviceType, sort, direction, 0);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"members.csv\"")
        .body(adminMemberService.exportMembersCsv(query));
  }

  @GetMapping("/admin/members/{memberId}")
  public String detail(@PathVariable UUID memberId, Model model) {
    model.addAttribute("member", adminMemberService.getMemberDetail(memberId));
    return "admin/member/detail";
  }
}
