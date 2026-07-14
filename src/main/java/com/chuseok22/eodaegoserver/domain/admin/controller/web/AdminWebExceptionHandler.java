package com.chuseok22.eodaegoserver.domain.admin.controller.web;

import com.chuseok22.eodaegoserver.global.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.RestClientException;

@Slf4j
@ControllerAdvice(basePackages = "com.chuseok22.eodaegoserver.domain.admin.controller.web")
public class AdminWebExceptionHandler {

  @ExceptionHandler(CustomException.class)
  public String handleCustomException(CustomException exception, Model model) {
    log.warn("[AdminWebExceptionHandler] 관리자 화면에서 CustomException 발생: {}", exception.getErrorCode());
    model.addAttribute("errorMessage", exception.getErrorCode().getMessage());
    return "admin/error";
  }

  @ExceptionHandler(RestClientException.class)
  public String handleRestClientException(RestClientException exception, Model model) {
    log.warn("[AdminWebExceptionHandler] 관리자 화면에서 AI 서버 호출 실패: {}", exception.getMessage());
    model.addAttribute("errorMessage", "AI 서버에 연결할 수 없습니다.");
    return "admin/error";
  }
}
