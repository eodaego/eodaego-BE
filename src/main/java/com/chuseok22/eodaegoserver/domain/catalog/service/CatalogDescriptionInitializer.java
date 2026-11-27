package com.chuseok22.eodaegoserver.domain.catalog.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

// 설명 파일은 배포 단위로 바뀌므로, 재배포만으로 기본값이 반영되도록 기동 시점에도 한 번 적용한다.
// 동기화로 새로 생긴 항목은 CatalogSyncWriter.write()가 동기화 직후에 같은 트랜잭션에서 반영한다.
@Component
@RequiredArgsConstructor
public class CatalogDescriptionInitializer implements ApplicationRunner {

  private final CatalogDescriptionService catalogDescriptionService;

  @Override
  public void run(ApplicationArguments args) {
    catalogDescriptionService.applyDefaultDescriptions();
  }
}
