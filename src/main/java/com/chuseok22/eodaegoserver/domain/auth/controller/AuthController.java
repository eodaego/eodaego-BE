package com.chuseok22.eodaegoserver.domain.auth.controller;

import com.chuseok22.eodaegoserver.domain.auth.dto.request.LoginRequest;
import com.chuseok22.eodaegoserver.domain.auth.dto.request.ReissueRequest;
import com.chuseok22.eodaegoserver.domain.auth.dto.response.TokenResponse;
import com.chuseok22.eodaegoserver.domain.auth.service.AuthService;
import com.chuseok22.logging.annotation.LogMonitoring;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController implements AuthControllerDocs {

  private final AuthService authService;

  @Override
  @LogMonitoring
  @PostMapping(path = "/login", version = "1")
  public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(authService.login(request));
  }

  @Override
  @LogMonitoring
  @PostMapping(path = "/reissue", version = "1")
  public ResponseEntity<TokenResponse> reissue(@Valid @RequestBody ReissueRequest request) {
    return ResponseEntity.ok(authService.reissue(request));
  }

  @Override
  @LogMonitoring
  @PostMapping(path = "/logout", version = "1")
  public ResponseEntity<Void> logout(@AuthenticationPrincipal UUID memberId) {
    authService.logout(memberId);
    return ResponseEntity.noContent().build();
  }
}
