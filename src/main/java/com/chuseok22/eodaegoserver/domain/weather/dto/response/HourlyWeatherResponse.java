package com.chuseok22.eodaegoserver.domain.weather.dto.response;

import com.chuseok22.eodaegoserver.domain.weather.dto.external.AiHourlyWeatherResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "시간대별 날씨 예보 응답")
public record HourlyWeatherResponse(

  @Schema(description = "예보 시각", example = "2026-07-18T16:00:00") LocalDateTime datetime,

  @Schema(description = "예상 기온", example = "28.0") Double temperature,

  @Schema(description = "강수 확률", example = "30") Integer precipitationProbability,

  @Schema(description = "강수 형태", example = "없음") String precipitationType,

  @Schema(description = "하늘 상태", example = "구름많음", nullable = true) String skyCondition

) {

  public static HourlyWeatherResponse from(AiHourlyWeatherResponse response) {
    return new HourlyWeatherResponse(response.datetime(), response.temperature(), response.precipitationProbability(), response.precipitationType(), response.skyCondition());
  }
}