package com.chuseok22.eodaegoserver.domain.weather.service;

import com.chuseok22.eodaegoserver.domain.weather.dto.response.WeatherResponse;
import com.chuseok22.eodaegoserver.global.exception.CustomException;
import com.chuseok22.eodaegoserver.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
@Slf4j
@RequiredArgsConstructor
public class WeatherService {
  private final RestClient aiServerRestClient;

  public WeatherResponse getCurrentWeather() {
    try {
      WeatherResponse response = aiServerRestClient
        .get()
        .uri("/api/v1/weather/current")
        .retrieve()
        .body(WeatherResponse.class);

      if (response == null) {
        log.warn("[WeatherService] AI 서버의 날씨 응답 본문이 비어 있습니다.");
        throw new CustomException(ErrorCode.AI_SERVER_UNAVAILABLE);
      }

      return response;
    } catch (RestClientException e) {
      log.warn("[WeatherService] AI 서버 날씨 조회에 실패했습니다. error={}", e.getMessage());
      throw new CustomException(ErrorCode.AI_SERVER_UNAVAILABLE);
    }
  }

}
