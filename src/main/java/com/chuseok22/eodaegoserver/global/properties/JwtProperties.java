package com.chuseok22.eodaegoserver.global.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
    String secretKey,
    long accessExpMillis,
    long refreshExpMillis,
    String issuer
) {
}
