package com.chuseok22.eodaegoserver.global.config;

import com.chuseok22.eodaegoserver.global.properties.AiServerProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(AiServerProperties.class)
public class AiServerConfig {

  private final AiServerProperties aiServerProperties;

  @Bean
  public RestClient aiServerRestClient() {
    // AI 서버가 응답하지 않을 때 요청이 무한 대기하지 않도록 connect/read 타임아웃을 명시적으로 설정한다.
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(aiServerProperties.connectTimeout());
    requestFactory.setReadTimeout(aiServerProperties.readTimeout());

    return RestClient.builder()
        .baseUrl(aiServerProperties.baseUrl())
        .requestFactory(requestFactory)
        .defaultHeader("X-Internal-Api-Key", aiServerProperties.internalApiKey())
        .build();
  }
}
