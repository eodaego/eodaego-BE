package com.chuseok22.eodaegoserver.domain.congestion;

public enum CongestionLevel {

  RELAXED,
  NORMAL,
  SLIGHTLY_CROWDED,
  CROWDED;

  public static CongestionLevel from(String rawText) {
    if (rawText == null) {
      return null;
    }
    return switch (rawText) {
      case "여유" -> RELAXED;
      case "보통" -> NORMAL;
      case "약간 붐빔" -> SLIGHTLY_CROWDED;
      case "붐빔" -> CROWDED;
      default -> null;
    };
  }
}
