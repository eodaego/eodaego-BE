package com.chuseok22.eodaegoserver.global.config;

import com.chuseok22.eodaegoserver.global.properties.SpringDocProperties;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
    info = @Info(
        title = "",
        description = """
            """,
        version = "1.0v"
    )
)
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(SpringDocProperties.class)
public class SwaggerConfig {

  private final SpringDocProperties properties;

  @Bean
  public OpenAPI openAPI() {
    SecurityScheme apiKey = new SecurityScheme()
        .type(Type.HTTP)
        .scheme("bearer")
        .bearerFormat("JWT")
        .in(In.HEADER)
        .name("Authorization");

    return new OpenAPI()
        .components(new Components().addSecuritySchemes("Bearer Token", apiKey))
        .addSecurityItem(new SecurityRequirement().addList("Bearer Token"));
  }

  @Bean
  public OpenApiCustomizer serverCustomizer() {
    return openApi -> {
      properties.servers().forEach(server ->
          openApi.addServersItem(new io.swagger.v3.oas.models.servers.Server()
              .url(server.url())
              .description(server.description()))
      );
    };
  }

  @Bean
  public OpenApiCustomizer apiVersionParameterCustomizer() {
    // ApiVersionConfig의 addPathPrefix("/api/{version}", ...)는 WebMvc 경로 매칭용 템플릿일 뿐
    // @PathVariable로 바인딩되지 않아 springdoc이 자동으로 인식하지 못한다.
    // 이 때문에 Swagger UI에서 version 입력 필드가 노출되지 않으므로 path parameter를 직접 등록한다.
    return openApi -> {
      if (openApi.getPaths() == null) {
        return;
      }
      openApi.getPaths().forEach((path, pathItem) -> {
        if (!path.startsWith("/api/")) {
          return;
        }
        pathItem.readOperations().forEach(operation -> {
          List<Parameter> parameters = operation.getParameters();
          if (parameters == null) {
            parameters = new ArrayList<>();
            operation.setParameters(parameters);
          }
          boolean alreadyHasVersion = parameters.stream()
              .anyMatch(parameter -> "version".equals(parameter.getName()));
          if (!alreadyHasVersion) {
            parameters.add(new Parameter()
                .in("path")
                .name("version")
                .required(true)
                .schema(new StringSchema())
                .example("1")
                .description("API 버전. 호출하려는 엔드포인트에 선언된 버전 값과 일치해야 한다."));
          }
        });
      });
    };
  }
}
