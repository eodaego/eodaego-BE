package com.chuseok22.eodaegoserver.global.config;

import com.chuseok22.eodaegoserver.global.properties.AiServerProperties;
import java.net.http.HttpClient;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(AiServerProperties.class)
public class AiServerConfig {

  private final AiServerProperties aiServerProperties;

  @Bean
  public RestClient aiServerRestClient() {
    // AI 서버(uvicorn)가 HTTP/1.1만 지원한다. HttpClient 기본값(HTTP/2)으로 두면 업그레이드 시도가 400으로 거부되므로 HTTP/1.1로 고정한다.
    HttpClient httpClient = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_1_1)
        .connectTimeout(aiServerProperties.connectTimeout())
        .build();
    JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
    requestFactory.setReadTimeout(aiServerProperties.readTimeout());

    return RestClient.builder()
        .baseUrl(aiServerProperties.baseUrl())
        .requestFactory(requestFactory)
        .defaultHeader("X-Internal-Api-Key", aiServerProperties.internalApiKey())
        .build();
  }
}
