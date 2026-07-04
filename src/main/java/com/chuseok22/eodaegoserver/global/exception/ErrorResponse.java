package com.chuseok22.eodaegoserver.global.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

  private ErrorCode errorCode;
  private String errorMessage;
  private List<FieldErrorDetail> fieldErrors;
}