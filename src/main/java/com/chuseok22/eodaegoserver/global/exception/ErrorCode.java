package com.chuseok22.eodaegoserver.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

  // GLOBAL

  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에 문제가 발생했습니다."),

  INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),

  ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근이 거부되었습니다."),

  // AUTH

  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),

  INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),

  REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "리프레시 토큰을 찾을 수 없습니다."),

  REFRESH_TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 일치하지 않거나 만료되었습니다."),

  FIREBASE_TOKEN_VERIFICATION_FAILED(HttpStatus.UNAUTHORIZED, "소셜 로그인 토큰 검증에 실패했습니다."),

  SOCIAL_TYPE_MISMATCH(HttpStatus.UNAUTHORIZED, "요청한 소셜 로그인 제공자와 실제 인증된 제공자가 일치하지 않습니다."),

  MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),

  // AI

  AI_SERVER_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "AI 서버에 연결할 수 없습니다."),

  // ADMIN

  AMUSEMENT_RIDE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 놀이기구입니다."),

  CRAWLING_SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 크롤링 스케줄입니다."),

  // Member

  NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),

  ;

  private final HttpStatus status;
  private final String message;
}