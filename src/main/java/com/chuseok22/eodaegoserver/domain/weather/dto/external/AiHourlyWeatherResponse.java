package com.chuseok22.eodaegoserver.domain.weather.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AiHourlyWeatherResponse(

  LocalDateTime datetime,

  Double temperature,

  @JsonProperty("precipitation_probability") Integer precipitationProbability,

  @JsonProperty("precipitation_type") String precipitationType,

  @JsonProperty("sky_condition") String skyCondition

) {

}