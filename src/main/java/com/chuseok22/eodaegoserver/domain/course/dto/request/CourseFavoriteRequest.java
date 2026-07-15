package com.chuseok22.eodaegoserver.domain.course.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CourseFavoriteRequest(

    @Schema(description = "즐겨찾기에 추가할 코스의 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @NotNull
    UUID courseId

) {

}
