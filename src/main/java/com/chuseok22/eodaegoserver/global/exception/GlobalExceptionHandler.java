package com.chuseok22.eodaegoserver.global.exception;

import io.jsonwebtoken.JwtException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
    log.warn("[MethodArgumentTypeMismatchException] 발생. parameter={}, value={}, requiredType={}",
        e.getName(), e.getValue(), resolveTypeName(e.getRequiredType()));

    return ResponseEntity.status(ErrorCode.INVALID_REQUEST.getStatus())
        .body(ErrorResponse.builder()
            .errorCode(ErrorCode.INVALID_REQUEST)
            .errorMessage(ErrorCode.INVALID_REQUEST.getMessage())
            .fieldErrors(List.of(new FieldErrorDetail(e.getName(), resolveMismatchReason(e.getRequiredType()))))
            .build());
  }

  private String resolveMismatchReason(Class<?> requiredType) {
    if (requiredType != null && requiredType.isEnum()) {
      String allowedValues = Arrays.stream(requiredType.getEnumConstants())
          .map(String::valueOf)
          .collect(Collectors.joining(", "));
      return "허용되지 않는 값입니다. 허용 값: " + allowedValues;
    }
    return "형식이 올바르지 않습니다. 필요한 형식: " + resolveTypeName(requiredType);
  }

  private String resolveTypeName(Class<?> requiredType) {
    return requiredType != null ? requiredType.getSimpleName() : "알 수 없음";
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

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException e) {
    log.warn("[DataIntegrityViolationException] 발생: {}", e.getMessage());
    return ResponseEntity.status(ErrorCode.DATA_INTEGRITY_VIOLATION.getStatus())
        .body(ErrorResponse.builder()
            .errorCode(ErrorCode.DATA_INTEGRITY_VIOLATION)
            .errorMessage(ErrorCode.DATA_INTEGRITY_VIOLATION.getMessage())
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
