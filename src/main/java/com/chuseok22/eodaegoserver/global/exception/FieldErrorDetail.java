package com.chuseok22.eodaegoserver.global.exception;

public record FieldErrorDetail(
  String field,
  String reason
) {

}
