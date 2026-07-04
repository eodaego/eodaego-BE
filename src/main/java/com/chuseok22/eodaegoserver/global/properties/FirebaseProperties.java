package com.chuseok22.eodaegoserver.global.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "firebase")
public record FirebaseProperties(
  String serviceAccountKeyPath
) {

}
