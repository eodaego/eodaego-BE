package com.chuseok22.eodaegoserver.domain.catalog.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AiPlantResponse(

    Long id,             // dedup 키
    String name,         // 이름
    String description,  // 설명
    @JsonProperty("thumbnail_url")
    String thumbnailUrl, // 이미지
    @JsonProperty("source_url")
    String sourceUrl      // 출처 링크

) {


}
