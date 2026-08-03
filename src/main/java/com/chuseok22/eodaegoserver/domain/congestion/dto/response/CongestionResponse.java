package com.chuseok22.eodaegoserver.domain.congestion.dto.response;

import com.chuseok22.eodaegoserver.domain.congestion.CongestionLevel;
import com.chuseok22.eodaegoserver.domain.congestion.dto.external.AiCongestionSnapshotResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "공원 현재 혼잡도 응답")
public record CongestionResponse(

  @Schema(description = "혼잡도 등급. RELAXED(여유) / NORMAL(보통) / SLIGHTLY_CROWDED(약간 붐빔) / CROWDED(붐빔) 네 가지만 내려간다. 서울시가 새로운 등급을 추가해 매핑되지 않는 값이 오면 null이며, 이때는 label을 그대로 표시하면 된다.",
    example = "RELAXED", nullable = true)
  CongestionLevel level,

  @Schema(description = "혼잡도 등급의 한글 표기. 서울시 원본 문자열을 그대로 전달하므로 level이 null인 상황에서도 항상 값이 있다.",
    example = "여유")
  String label,

  @Schema(description = "AI 서버가 이 데이터를 수집한 시각(KST). 서울시 데이터의 기준 시각과는 차이가 있을 수 있어 '현재 시각'으로 해석하면 안 된다.",
    example = "2026-08-03T15:20:00")
  LocalDateTime collectedAt

) {

  public static CongestionResponse from(AiCongestionSnapshotResponse response) {
    return new CongestionResponse(
      CongestionLevel.from(response.congestionLevel()),
      response.congestionLevel(),
      response.collectedAt()
    );
  }
}
