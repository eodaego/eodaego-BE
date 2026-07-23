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
        log.warn("AI 서버의 날씨 응답 본문이 비어 있습니다.");
        throw new CustomException(ErrorCode.AI_SERVER_UNAVAILABLE);
      }

      return response;
    } catch (RestClientException e) {
      log.warn("AI 서버 날씨 조회에 실패했습니다. uri={}, error={}", CURRENT_WEATHER_URI, e.getMessage());

      throw new CustomException(ErrorCode.AI_SERVER_UNAVAILABLE);
    }
  }
}