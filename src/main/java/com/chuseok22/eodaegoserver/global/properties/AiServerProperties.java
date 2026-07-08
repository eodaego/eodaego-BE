package com.chuseok22.eodaegoserver.global.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai-server")
public record AiServerProperties(
    String baseUrl,
    String internalApiKey
) {
}
