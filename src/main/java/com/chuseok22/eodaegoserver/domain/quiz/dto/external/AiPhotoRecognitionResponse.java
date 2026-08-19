package com.chuseok22.eodaegoserver.domain.quiz.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AiPhotoRecognitionResponse(

    @JsonProperty("catalog_id")
    Long catalogId,   // == externalId, 인식 실패면 null

    String name

) {
}
