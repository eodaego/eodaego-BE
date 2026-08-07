package com.chuseok22.eodaegoserver.domain.catalog.dto.request;

import com.chuseok22.eodaegoserver.domain.catalog.CatalogItemStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * 도감 항목 수정 요청.
 * xxxOverride는 "AI 원본 대신 쓸 값"을 뜻하며, null을 보내면 그 필드는 AI 원본을 따른다(원복).
 */
public record CatalogItemUpdateRequest(

    @Schema(description = "AI 원본 이름 대신 사용할 이름. null이면 AI 원본 이름을 그대로 쓴다(원복).",
        example = "사자")
    String nameOverride,

    @Schema(description = "AI 원본 설명 대신 사용할 특징 설명. null이면 AI 원본 설명을 그대로 쓴다(원복).",
        example = "무리를 지어 사는 대형 고양잇과 동물이다.")
    String featureOverride,

    @Schema(description = "어린이 눈높이 설명. AI가 제공하지 않는 값이라 관리자가 직접 작성한다. "
        + "아직 작성하지 않았다면 빈 문자열(\"\")을 보낸다. 동기화로 생성된 항목도 빈 문자열로 시작한다.",
        example = "사자는 갈기가 멋진 초원의 왕이에요!")
    @NotNull
    String childDescription,

    @Schema(description = "공개 상태. AVAILABLE(수집 가능), SUSPENDED(임시 중단), RETIRED(운영 종료)만 허용된다.",
        example = "AVAILABLE")
    @NotNull
    CatalogItemStatus status,

    @Schema(description = "AI 원본 이미지 대신 사용할 이미지 URL. null이면 AI 원본 이미지를 그대로 쓴다(원복).",
        example = "https://cdn.eodaego.com/animals/lion.png")
    String imageUrlOverride,

    @Schema(description = "AI 원본 위도 대신 사용할 위도. 장소(PLACE) 전용이며 null이면 AI 원본 좌표를 그대로 쓴다(원복).",
        example = "37.5498")
    Double latitudeOverride,

    @Schema(description = "AI 원본 경도 대신 사용할 경도. 장소(PLACE) 전용이며 null이면 AI 원본 좌표를 그대로 쓴다(원복).",
        example = "127.0731")
    Double longitudeOverride

) {

}
