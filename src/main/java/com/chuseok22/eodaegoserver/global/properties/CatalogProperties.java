package com.chuseok22.eodaegoserver.global.properties;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "catalog")
public record CatalogProperties(
    Duration syncLockTtl
) {

}
