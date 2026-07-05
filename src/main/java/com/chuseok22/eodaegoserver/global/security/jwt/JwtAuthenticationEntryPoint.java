package com.chuseok22.eodaegoserver.global.security.jwt;

import com.chuseok22.eodaegoserver.global.exception.ErrorCode;
import com.chuseok22.eodaegoserver.global.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper;

  @Override
  public void commence(
    HttpServletRequest request,
    HttpServletResponse response,
    AuthenticationException authException
  ) throws IOException {
    response.setStatus(ErrorCode.UNAUTHORIZED.getStatus().value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    ErrorResponse errorResponse = ErrorResponse.builder()
      .errorCode(ErrorCode.UNAUTHORIZED)
      .errorMessage(ErrorCode.UNAUTHORIZED.getMessage())
      .build();
    response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
  }
}
