package com.chuseok22.eodaegoserver.global.security.jwt;

import com.chuseok22.eodaegoserver.global.exception.ErrorCode;
import com.chuseok22.eodaegoserver.global.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

  private final ObjectMapper objectMapper;

  @Override
  public void handle(
    HttpServletRequest request,
    HttpServletResponse response,
    AccessDeniedException accessDeniedException
  ) throws IOException {
    response.setStatus(ErrorCode.ACCESS_DENIED.getStatus().value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    ErrorResponse errorResponse = ErrorResponse.builder()
      .errorCode(ErrorCode.ACCESS_DENIED)
      .errorMessage(ErrorCode.ACCESS_DENIED.getMessage())
      .build();
    response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
  }
}
