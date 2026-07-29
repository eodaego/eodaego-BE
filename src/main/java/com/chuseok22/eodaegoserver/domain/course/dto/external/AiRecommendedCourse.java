package com.chuseok22.eodaegoserver.domain.course.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AiRecommendedCourse(

    String title,             // 코스 제목

    @JsonProperty("estimated_duration_minutes")
    Integer estimatedDurationMinutes,       // 코스 예상 소요시간(분)

    List<AiRouteStop> stops,

    @JsonProperty("tag_labels")
    List<String> tagLabels

) {

}
