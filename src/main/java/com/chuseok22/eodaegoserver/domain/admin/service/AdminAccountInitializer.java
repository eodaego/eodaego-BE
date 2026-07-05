package com.chuseok22.eodaegoserver.domain.admin.service;

import com.chuseok22.eodaegoserver.domain.admin.entity.Admin;
import com.chuseok22.eodaegoserver.domain.admin.repository.AdminRepository;
import com.chuseok22.eodaegoserver.global.properties.AdminAccountProperties;
import com.chuseok22.eodaegoserver.global.security.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(AdminAccountProperties.class)
public class AdminAccountInitializer implements ApplicationRunner {

  private final AdminAccountProperties adminAccountProperties;
  private final AdminRepository adminRepository;
  private final PasswordEncoder passwordEncoder;;

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    adminAccountProperties.accounts().forEach(account ->
        adminRepository.findByUsername(account.username())
            .ifPresentOrElse(
                existing -> existing.setPassword(passwordEncoder.encode(account.password())),
                () -> adminRepository.save(
                    Admin.builder()
                        .username(account.username())
                        .password(passwordEncoder.encode(account.password()))
                        .role(Role.ADMIN)
                        .build()
                )
            )
    );
  }
}
