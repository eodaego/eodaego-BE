package com.chuseok22.eodaegoserver.domain.weather.dto.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "현재 날씨 및 시간대별 예보 응답")
public record WeatherResponse (

  @Schema(description = "날씨 데이터 식별자", example = "1")
  Long id,

  @JsonAlias("place_ref_key")
  @Schema(description = "장소 참조 키", example = "children_grand_park")
  String placeRefKey,

  @Schema(description = "현재 기온", example = "28.5")
  Double temperature,

  @Schema(description = "현재 습도", example = "70")
  Integer humidity,

  @JsonAlias("precipitation_type")
  @Schema(description = "강수 형태", example = "없음")
  String precipitationType,

  @JsonAlias("wind_speed")
  @Schema(description = "풍속", example = "2.3")
  Double windSpeed,

  @JsonAlias("sky_condition")
  @Schema(description = "하늘 상태", example = "맑음", nullable = true)
  String skyCondition,

  @JsonAlias("hourly_forecast")
  @Schema(description = "시간대별 날씨 예보")
  List<HourlyWeatherResponse> hourlyForecast,

  @JsonAlias("observed_at")
  @Schema(description = "날씨 관측 시각", example = "2026-07-18T15:00:00")
  LocalDateTime observedAt,

  @JsonAlias("collected_at")
  @Schema(description = "데이터 수집 시각", example = "2026-07-18T15:10:00")
  LocalDateTime collectedAt

){
}
