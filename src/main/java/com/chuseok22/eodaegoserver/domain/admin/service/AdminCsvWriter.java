package com.chuseok22.eodaegoserver.domain.admin.service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public final class AdminCsvWriter {

  private static final String BYTE_ORDER_MARK = "﻿";
  private static final String LINE_SEPARATOR = "\r\n";
  private static final String FORMULA_TRIGGER_CHARACTERS = "=+-@\t\r";

  private AdminCsvWriter() {
  }

  // BOM이 없으면 Excel이 UTF-8 한글을 깨뜨려 연다.
  public static byte[] write(List<String> header, List<List<String>> rows) {
    StringBuilder builder = new StringBuilder(BYTE_ORDER_MARK);
    builder.append(joinLine(header));
    for (List<String> row : rows) {
      builder.append(LINE_SEPARATOR).append(joinLine(row));
    }
    return builder.toString().getBytes(StandardCharsets.UTF_8);
  }

  private static String joinLine(List<String> fields) {
    return fields.stream().map(AdminCsvWriter::escapeField).collect(Collectors.joining(","));
  }

  private static String escapeField(String value) {
    String safeValue = value == null ? "" : value;
    // 스프레드시트가 수식으로 해석하지 못하도록 앞에 작은따옴표를 붙인다.
    if (!safeValue.isEmpty() && FORMULA_TRIGGER_CHARACTERS.indexOf(safeValue.charAt(0)) >= 0) {
      safeValue = "'" + safeValue;
    }
    if (safeValue.contains(",") || safeValue.contains("\"") || safeValue.contains("\n") || safeValue.contains("\r")) {
      return "\"" + safeValue.replace("\"", "\"\"") + "\"";
    }
    return safeValue;
  }
}
