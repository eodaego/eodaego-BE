package com.chuseok22.eodaegoserver.domain.weather.controller;

import com.chuseok22.eodaegoserver.domain.weather.dto.response.WeatherResponse;
import com.chuseok22.eodaegoserver.domain.weather.service.WeatherService;
import com.chuseok22.logging.annotation.LogMonitoring;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/weather")
@RequiredArgsConstructor
public class WeatherController implements WeatherControllerDocs {
  private final WeatherService weatherService;

  @Override
  @LogMonitoring
  @GetMapping(path = "/current", version = "1")
  public ResponseEntity<WeatherResponse> getCurrentWeather() {
    return ResponseEntity.ok(weatherService.getCurrentWeather());
  }
}
