package com.chuseok22.eodaegoserver.domain.weather.dto.response;

import com.chuseok22.eodaegoserver.domain.weather.dto.external.AiWeatherResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "현재 날씨 및 시간대별 예보 응답")
public record WeatherResponse(

  @Schema(description = "날씨 데이터 식별자", example = "1") Long id,

  @Schema(description = "장소 참조 키", example = "children_grand_park") String placeRefKey,

  @Schema(description = "현재 기온", example = "28.5") Double temperature,

  @Schema(description = "현재 습도", example = "70") Integer humidity,

  @Schema(description = "강수 형태", example = "없음") String precipitationType,

  @Schema(description = "풍속", example = "2.3") Double windSpeed,

  @Schema(description = "하늘 상태", example = "맑음", nullable = true) String skyCondition,

  @Schema(description = "시간대별 날씨 예보") List<HourlyWeatherResponse> hourlyForecast,

  @Schema(description = "날씨 관측 시각", example = "2026-07-18T15:00:00") LocalDateTime observedAt,

  @Schema(description = "데이터 수집 시각", example = "2026-07-18T15:10:00") LocalDateTime collectedAt

) {

  public static WeatherResponse from(AiWeatherResponse response) {
    List<HourlyWeatherResponse> hourlyForecast = response.hourlyForecast() == null ? List.of() : response.hourlyForecast().stream().map(HourlyWeatherResponse::from).toList();

    return new WeatherResponse(response.id(), response.placeRefKey(), response.temperature(), response.humidity(), response.precipitationType(), response.windSpeed(), response.skyCondition(),
      hourlyForecast, response.observedAt(), response.collectedAt());
  }
}