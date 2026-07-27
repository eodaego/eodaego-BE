package com.chuseok22.eodaegoserver.domain.course.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CourseFavoriteRequest(

  @NotNull
  @Schema(description = "즐겨찾기에 저장할 코스 ID", example = "9c858901-8a57-4791-81fe-4c455b099bc9")
  UUID courseId

) {

}
