package com.chuseok22.eodaegoserver.domain.course.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AiRouteRecommendationResponse(

    List<AiRecommendedCourse> courses  // 추천 코스 목록

) {

}
