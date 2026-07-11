package com.chuseok22.eodaegoserver.domain.member.controller;

import com.chuseok22.eodaegoserver.domain.member.dto.request.AgreementRequest;
import com.chuseok22.eodaegoserver.domain.member.dto.response.AgreementResponse;
import com.chuseok22.eodaegoserver.domain.member.service.MemberService;
import com.chuseok22.logging.annotation.LogMonitoring;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController implements MemberControllerDocs {

  private final MemberService memberService;

  @Override
  @LogMonitoring
  @GetMapping(path = "/me/agreements", version = "1")
  public ResponseEntity<AgreementResponse> getAgreement(
      @AuthenticationPrincipal UUID memberId
  ) {
    return ResponseEntity.ok(memberService.getAgreement(memberId));
  }

  @Override
  @LogMonitoring
  @PatchMapping(path = "/me/agreements", version = "1")
  public ResponseEntity<Void> updateAgreement(
      @AuthenticationPrincipal UUID memberId,
      @Valid @RequestBody AgreementRequest request
  ) {
    memberService.updateAgreement(memberId, request);
    return ResponseEntity.noContent().build();
  }

}
