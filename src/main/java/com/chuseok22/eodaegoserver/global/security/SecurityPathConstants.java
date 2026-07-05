package com.chuseok22.eodaegoserver.global.security;

public final class SecurityPathConstants {

  private SecurityPathConstants() {
  }

  public static final String[] AUTH_PERMIT_ALL_PATHS = {
      "/api/*/auth/login",
      "/api/*/auth/reissue"
  };

  public static final String ADMIN_LOGIN_PATH = "/admin/login";

  public static final String[] SWAGGER_PERMIT_ALL_PATHS = {
      "/docs/swagger",
      "/docs/swagger/**",
      "/docs/swagger-ui/**",
      "/v3/api-docs",
      "/v3/api-docs/**",
      "/v3/api-docs.yaml"
  };
}
