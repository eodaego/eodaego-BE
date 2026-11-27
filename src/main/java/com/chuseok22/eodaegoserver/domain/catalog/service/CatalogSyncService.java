package com.chuseok22.eodaegoserver.domain.catalog.service;

import com.chuseok22.eodaegoserver.domain.catalog.CatalogCategory;
import com.chuseok22.eodaegoserver.domain.catalog.dto.external.AiAnimalResponse;
import com.chuseok22.eodaegoserver.domain.catalog.dto.external.AiPlantResponse;
import com.chuseok22.eodaegoserver.domain.facility.dto.external.AiFacilityResponse;
import com.chuseok22.eodaegoserver.domain.facility.service.FacilitySyncService;
import com.chuseok22.eodaegoserver.global.exception.CustomException;
import com.chuseok22.eodaegoserver.global.exception.ErrorCode;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 도감 동기화의 진입점. AI 서버 조회를 트랜잭션 밖에서 끝낸 뒤 {@link CatalogSyncWriter}에 넘긴다.
 *
 * <p>이 클래스에는 {@code @Transactional}을 붙이지 않는다. 붙이는 순간 AI 서버 응답을 기다리는
 * 동안 DB 커넥션이 묶인다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogSyncService {

  // 동기화 한 번은 AI 서버 호출만으로 수 분이 걸릴 수 있다. 겹쳐 돌면 그만큼 외부 호출과 쓰기
  // 트랜잭션이 쌓이므로, 이미 돌고 있으면 기다리지 않고 바로 돌려보낸다.
  private final CatalogSyncLock catalogSyncLock;
  private final CatalogAiClient catalogAiClient;
  private final FacilitySyncService facilitySyncService;
  private final CatalogSyncWriter catalogSyncWriter;

  public CatalogSyncResult syncFromAiServer() {
    String lockToken = catalogSyncLock.tryAcquire().orElseThrow(() -> {
      log.info("도감 동기화 중복 실행 차단. 이미 실행 중인 작업이 있습니다.");
      return new CustomException(ErrorCode.CATALOG_SYNC_ALREADY_RUNNING);
    });

    try {
      List<ExternalCatalogData> externals = fetchExternals();
      List<AiFacilityResponse> facilities = facilitySyncService.fetchExternals();
      return catalogSyncWriter.write(externals, facilities);
    } finally {
      catalogSyncLock.release(lockToken);
    }
  }

  private List<ExternalCatalogData> fetchExternals() {
    List<ExternalCatalogData> externals = new ArrayList<>();

    for (AiAnimalResponse animal : catalogAiClient.fetchAnimals()) {
      externals.add(new ExternalCatalogData(CatalogCategory.ANIMAL, animal.id(), animal.name(),
          animal.thumbnailUrl(), null));
    }

    for (AiPlantResponse plant : catalogAiClient.fetchPlants()) {
      externals.add(new ExternalCatalogData(CatalogCategory.PLANT, plant.id(), plant.name(),
          plant.thumbnailUrl(), plant.description()));
    }

    return externals;
  }
}
