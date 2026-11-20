package com.chuseok22.eodaegoserver.domain.admin.dto.response;

import java.time.LocalDate;

public record DailyCountView(
    LocalDate date,
    long count
) {

}
