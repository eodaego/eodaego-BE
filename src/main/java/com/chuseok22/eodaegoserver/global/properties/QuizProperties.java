package com.chuseok22.eodaegoserver.global.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

@ConfigurationProperties(prefix = "quiz")
public record QuizProperties(
    long answerTtlMillis,
    DataSize maxImageSize
) {

}
