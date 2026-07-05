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
    configurer.usePathSegment(1);
  }
}
