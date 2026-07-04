package com.chuseok22.eodaegoserver.global.properties;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "admin")
public record AdminAccountProperties(
  List<AdminAccount> accounts
) {

  public AdminAccountProperties {
    accounts = accounts == null ? List.of() : accounts;
  }

  public record AdminAccount(String username, String password) {

  }
}
