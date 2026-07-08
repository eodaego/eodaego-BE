package com.chuseok22.eodaegoserver.global.config;

import com.chuseok22.eodaegoserver.global.properties.AiServerProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(AiServerProperties.class)
public class AiServerConfig {

  private final AiServerProperties aiServerProperties;

  @Bean
  public RestClient aiServerRestClient() {
    return RestClient.builder()
        .baseUrl(aiServerProperties.baseUrl())
        .defaultHeader("X-Internal-Api-Key", aiServerProperties.internalApiKey())
        .build();
  }
}
