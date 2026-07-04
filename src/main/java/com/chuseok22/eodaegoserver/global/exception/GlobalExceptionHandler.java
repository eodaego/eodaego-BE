package com.chuseok22.eodaegoserver.global.exception;

import io.jsonwebtoken.JwtException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(annotations = RestController.class)
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(CustomException.class)
  public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
    log.warn("[CustomException] 발생: {}", e.getMessage());
    ErrorCode errorCode = e.getErrorCode();
    return ResponseEntity.status(errorCode.getStatus())
        .body(ErrorResponse.builder()
            .errorCode(errorCode)
            .errorMessage(errorCode.getMessage())
            .build());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
    log.warn("[MethodArgumentNotValidException] 발생");
    List<FieldErrorDetail> fieldErrors = e.getBindingResult().getFieldErrors().stream()
        .map(fieldError -> new FieldErrorDetail(fieldError.getField(), fieldError.getDefaultMessage()))
        .toList();

    return ResponseEntity.status(ErrorCode.INVALID_REQUEST.getStatus())
        .body(ErrorResponse.builder()
            .errorCode(ErrorCode.INVALID_REQUEST)
            .errorMessage(ErrorCode.INVALID_REQUEST.getMessage())
            .fieldErrors(fieldErrors)
            .build());
  }

  @ExceptionHandler(JwtException.class)
  public ResponseEntity<ErrorResponse> handleJwtException(JwtException e) {
    log.warn("[JwtException] 발생");
    return ResponseEntity.status(ErrorCode.INVALID_TOKEN.getStatus())
        .body(ErrorResponse.builder()
            .errorCode(ErrorCode.INVALID_TOKEN)
            .errorMessage(ErrorCode.INVALID_TOKEN.getMessage())
            .build());
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
    log.warn("[HttpMessageNotReadableException] 발생");
    return ResponseEntity.status(ErrorCode.INVALID_REQUEST.getStatus())
        .body(ErrorResponse.builder()
            .errorCode(ErrorCode.INVALID_REQUEST)
            .errorMessage(ErrorCode.INVALID_REQUEST.getMessage())
            .build());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception e) {
    log.error("처리되지 않은 예외 발생: {}", e.getMessage());
    return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
        .body(ErrorResponse.builder()
            .errorCode(ErrorCode.INTERNAL_SERVER_ERROR)
            .errorMessage(ErrorCode.INTERNAL_SERVER_ERROR.getMessage())
            .build());
  }
}
