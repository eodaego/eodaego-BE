package com.chuseok22.eodaegoserver.domain.course.dto.response;

import java.util.UUID;

public record CoursePlaceCatalogInfo(

    UUID catalogItemId,

    boolean collected

) {
}
