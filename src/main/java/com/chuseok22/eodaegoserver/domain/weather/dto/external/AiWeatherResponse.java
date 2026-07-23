package com.chuseok22.eodaegoserver.domain.weather.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AiWeatherResponse(

  Long id,

  @JsonProperty("place_ref_key") String placeRefKey,

  Double temperature,

  Integer humidity,

  @JsonProperty("precipitation_type") String precipitationType,

  @JsonProperty("wind_speed") Double windSpeed,

  @JsonProperty("sky_condition") String skyCondition,

  @JsonProperty("hourly_forecast") List<AiHourlyWeatherResponse> hourlyForecast,

  @JsonProperty("observed_at") LocalDateTime observedAt,

  @JsonProperty("collected_at") LocalDateTime collectedAt

) {

}