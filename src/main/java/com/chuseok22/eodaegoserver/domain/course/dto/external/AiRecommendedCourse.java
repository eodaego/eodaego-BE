package com.chuseok22.eodaegoserver.domain.course.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AiRecommendedCourse(

    String title,             // 코스 제목

    String reason,             // 이 코스를 추천하는 이유

    @JsonProperty("duration_minutes")
    int durationMinutes,       // 코스 실제 소요시간(분)

    List<AiRouteStop> stops

) {

}
