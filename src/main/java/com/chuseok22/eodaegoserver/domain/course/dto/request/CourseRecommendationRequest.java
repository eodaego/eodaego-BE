package com.chuseok22.eodaegoserver.domain.course.dto.request;

import com.chuseok22.eodaegoserver.domain.course.CompanionType;
import com.chuseok22.eodaegoserver.domain.course.EntranceGate;
import com.chuseok22.eodaegoserver.domain.course.InterestType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record CourseRecommendationRequest(

    @Schema(description = "관심 태그 목록. 건너뛰면 null이며, 이 경우 전체 태그를 사용한다.", example = "[\"ANIMAL\", \"ACTIVITY\"]")
    List<InterestType> preferenceTags,

    @Schema(description = "예상 체류 시간(분). 건너뛰면 null이며, 이 경우 기본값(1440분, 24시간)을 사용한다.", example = "120")
    @Positive
    Integer stayDurationMinutes,

    @Schema(description = "입구로 사용할 출입문. 건너뛰기 불가(필수).", example = "MAIN_GATE")
    @NotNull
    EntranceGate entrance,

    @Schema(description = "출구로 사용할 출입문. 건너뛰기 불가(필수).", example = "SOUTH_GATE")
    @NotNull
    EntranceGate exit,

    @Schema(description = "동행 유형. 건너뛰면 null이며, 이 경우 기본값(ALONE)을 사용한다.", example = "WITH_CHILD")
    CompanionType companionType

) {

}
