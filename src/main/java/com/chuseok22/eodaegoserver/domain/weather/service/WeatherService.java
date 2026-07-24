package com.chuseok22.eodaegoserver.domain.weather.service;

import com.chuseok22.eodaegoserver.domain.weather.dto.external.AiWeatherResponse;
import com.chuseok22.eodaegoserver.domain.weather.dto.response.WeatherResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WeatherService {

  private final WeatherAiClient weatherAiClient;

  public WeatherResponse getCurrentWeather() {
    AiWeatherResponse aiWeatherResponse = weatherAiClient.fetchCurrentWeather();

    return WeatherResponse.from(aiWeatherResponse);
  }
}