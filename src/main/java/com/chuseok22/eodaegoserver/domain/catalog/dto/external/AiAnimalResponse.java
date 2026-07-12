package com.chuseok22.eodaegoserver.domain.catalog.dto.external;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AiAnimalResponse(

    Long id,               // dedup 키
    String name,           // 이름
    @JsonProperty("scientific_name")
    String scientificName, // 학명
    @JsonProperty("english_name")
    String englishName,    // 영문명
    String classification, // 분류
    String distribution,   // 분포
    String diet,           // 먹이
    @JsonProperty("location_name")
    String locationName,   // 서식지
    @JsonProperty("thumbnail_url")
    String thumbnailUrl,   // 이미지
    @JsonProperty("source_url")
    String sourceUrl       // 출처 링크

) {

}