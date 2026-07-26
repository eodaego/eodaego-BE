package com.chuseok22.eodaegoserver.domain.course;

public enum EntranceGate {
  MAIN_GATE(37.548042, 127.074766),       // 정문
  HOEGWAN_GATE(37.545787, 127.075568),    // 회관문
  SOUTH_GATE(37.544401, 127.080086),      // 남문
  GUI_GATE(37.545950, 127.087362),        // 구의문
  EAST_GATE_1(37.547227, 127.089257),     // 동문1
  EAST_GATE_2(37.548708, 127.089555),     // 동문2
  REAR_GATE(37.551206, 127.088769),       // 후문
  NORTH_GATE_1(37.552337, 127.083347),    // 북문1
  NORTH_GATE_2(37.552617, 127.080908),    // 북문2
  WEST_GATE(37.551120, 127.076510),       // 서문
  NEUNGDONG_GATE(37.546895, 127.074286);  // 능동문

  private final double latitude;
  private final double longitude;

  EntranceGate(double latitude, double longitude) {
    this.latitude = latitude;
    this.longitude = longitude;
  }
}
