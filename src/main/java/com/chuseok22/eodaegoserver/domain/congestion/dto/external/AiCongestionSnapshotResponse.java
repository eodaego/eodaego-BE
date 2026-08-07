package com.chuseok22.eodaegoserver.domain.congestion.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AiCongestionSnapshotResponse(

  @JsonProperty("congestion_level") String congestionLevel,

  @JsonProperty("collected_at") LocalDateTime collectedAt

) {

}
