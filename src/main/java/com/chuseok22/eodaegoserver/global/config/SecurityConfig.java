package com.chuseok22.eodaegoserver.global.config;

import com.chuseok22.eodaegoserver.domain.admin.service.AdminDetailsService;
import com.chuseok22.eodaegoserver.global.properties.JwtProperties;
import com.chuseok22.eodaegoserver.global.security.SecurityPathConstants;
import com.chuseok22.eodaegoserver.global.security.jwt.JwtAccessDeniedHandler;
import com.chuseok22.eodaegoserver.global.security.jwt.JwtAuthenticationEntryPoint;
import com.chuseok22.eodaegoserver.global.security.jwt.JwtAuthenticationFilter;
import com.chuseok22.eodaegoserver.global.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

  private final JwtProvider jwtProvider;
  private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
  private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
  private final AdminDetailsService adminDetailsService;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  @Order(1)
  public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
    http
        .securityMatcher("/api/**")
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.POST, SecurityPathConstants.AUTH_PERMIT_ALL_PATHS).permitAll()
            .anyRequest().authenticated())
        .exceptionHandling(eh -> eh
            .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            .accessDeniedHandler(jwtAccessDeniedHandler))
        .addFilterBefore(new JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  @Order(2)
  public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
    http
        .securityMatcher("/admin/**")
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(SecurityPathConstants.ADMIN_LOGIN_PATH).permitAll()
            .anyRequest().hasRole("ADMIN"))
        .userDetailsService(adminDetailsService)
        .formLogin(form -> form
            .loginPage(SecurityPathConstants.ADMIN_LOGIN_PATH)
            .defaultSuccessUrl("/admin/dashboard", true)
            .permitAll())
        .logout(logout -> logout
            .logoutUrl("/admin/logout")
            .logoutSuccessUrl(SecurityPathConstants.ADMIN_LOGIN_PATH)
            .permitAll());

    return http.build();
  }

  @Bean
  @Order(0)
  public SecurityFilterChain swaggerSecurityFilterChain(HttpSecurity http) throws Exception {
    http
        .securityMatcher(SecurityPathConstants.SWAGGER_PERMIT_ALL_PATHS)
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

    return http.build();
  }
}
