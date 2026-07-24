package com.chuseok22.eodaegoserver.domain.weather.service;

import com.chuseok22.eodaegoserver.domain.weather.dto.external.AiWeatherResponse;
import com.chuseok22.eodaegoserver.global.exception.CustomException;
import com.chuseok22.eodaegoserver.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherAiClient {

  private static final String CURRENT_WEATHER_URI = "/api/v1/weather/current";

  private final RestClient aiServerRestClient;

  public AiWeatherResponse fetchCurrentWeather() {
    try {
      AiWeatherResponse response = aiServerRestClient.get().uri(CURRENT_WEATHER_URI).retrieve().body(AiWeatherResponse.class);

      if (response == null) {
        log.error("AI 서버 날씨 응답 본문이 비어 있습니다. uri={}", CURRENT_WEATHER_URI);
        throw new CustomException(ErrorCode.AI_SERVER_UNAVAILABLE);
      }

      return response;
    } catch (RestClientException exception) {
      log.error("AI 서버 날씨 조회 실패. uri={}", CURRENT_WEATHER_URI, exception);
      throw new CustomException(ErrorCode.AI_SERVER_UNAVAILABLE);
    }
  }
}