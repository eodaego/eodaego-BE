package com.chuseok22.eodaegoserver.domain.admin.dto.response;

import java.util.List;

public record AiModelListResponseView(
    List<AiModelSummaryView> models
) {
}
