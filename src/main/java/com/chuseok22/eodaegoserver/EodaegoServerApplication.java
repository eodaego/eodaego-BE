package com.chuseok22.eodaegoserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class EodaegoServerApplication {

  public static void main(String[] args) {
    SpringApplication.run(EodaegoServerApplication.class, args);
  }

}
