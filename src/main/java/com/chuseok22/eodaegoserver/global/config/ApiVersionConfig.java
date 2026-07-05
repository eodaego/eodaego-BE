package com.chuseok22.eodaegoserver.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.method.HandlerTypePredicate;

@Configuration
public class ApiVersionConfig implements WebMvcConfigurer {

  private static final String BASE_PACKAGE = "com.chuseok22.eodaegoserver";

  @Override
  public void configurePathMatch(PathMatchConfigurer configurer) {
    configurer.addPathPrefix(
        "/api/{version}",
        HandlerTypePredicate.forBasePackage(BASE_PACKAGE)
            .and(HandlerTypePredicate.forAnnotation(RestController.class))
    );
  }

  @Override
  public void configureApiVersioning(ApiVersionConfigurer configurer) {
    // 버전 파싱 대상을 /api/**로 한정하지 않으면 관리자·Swagger 등 다른 모든 요청의 두 번째 경로 조각까지
    // API 버전으로 해석을 시도해 InvalidApiVersionException(400)을 유발한다. versionRequired를 false로 두어
    // /api/** 외 요청은 버전 미해석 상태로 통과시키며, /api/** 요청은 predicate가 true이므로 영향받지 않는다.
    configurer.usePathSegment(1, path -> path.pathWithinApplication().value().startsWith("/api/"))
        .setVersionRequired(false);
  }
}
