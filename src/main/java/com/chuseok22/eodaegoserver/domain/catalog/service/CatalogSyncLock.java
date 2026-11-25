package com.chuseok22.eodaegoserver.domain.catalog.service;

import com.chuseok22.eodaegoserver.global.properties.CatalogProperties;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

/**
 * 도감 동기화의 중복 실행을 막는 분산 잠금.
 *
 * <p>인스턴스 안에서만 유효한 잠금으로는 부족하다. Docker Swarm 롤링 업데이트 중에는 replicas가 1이어도
 * 새 컨테이너와 기존 컨테이너가 잠시 함께 떠 있어, 그 사이에 동기화가 겹쳐 돌 수 있다.
 */
@Slf4j
@Component
@EnableConfigurationProperties(CatalogProperties.class)
public class CatalogSyncLock {

  private static final String KEY = "catalog:sync:lock";

  // 해제는 "내 토큰일 때만 삭제"여야 한다. get 후 delete로 나눠 쓰면 그 사이에 TTL이 만료되어
  // 다음 실행자가 잡은 잠금을 지워 버릴 수 있다. Lua는 원자적으로 실행되므로 그 틈이 없다.
  private static final RedisScript<Long> RELEASE_SCRIPT = new DefaultRedisScript<>(
      "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
      Long.class);

  private final StringRedisTemplate redisTemplate;
  private final CatalogProperties catalogProperties;

  public CatalogSyncLock(StringRedisTemplate redisTemplate, CatalogProperties catalogProperties) {
    this.redisTemplate = redisTemplate;
    this.catalogProperties = catalogProperties;
  }

  /**
   * 잠금을 얻으면 해제용 토큰을 돌려준다. 이미 실행 중이면 기다리지 않고 비어 있는 값을 돌려준다.
   *
   * <p>TTL은 한 번의 동기화가 끝나기에 충분히 길어야 한다. 짧으면 아직 도는 중에 잠금이 풀려
   * 두 실행이 겹친다. 반대로 프로세스가 죽어도 TTL이 지나면 잠금이 저절로 풀린다.
   */
  public Optional<String> tryAcquire() {
    String token = UUID.randomUUID().toString();
    Boolean acquired = redisTemplate.opsForValue()
        .setIfAbsent(KEY, token, catalogProperties.syncLockTtl());

    if (!Boolean.TRUE.equals(acquired)) {
      return Optional.empty();
    }

    log.debug("도감 동기화 잠금 획득. token={}, ttl={}", token, catalogProperties.syncLockTtl());
    return Optional.of(token);
  }

  public void release(String token) {
    Long released = redisTemplate.execute(RELEASE_SCRIPT, List.of(KEY), token);

    if (released == null || released == 0L) {
      // TTL이 먼저 만료됐다는 뜻이다. 동기화가 TTL보다 오래 걸렸을 수 있으니 설정값을 점검해야 한다.
      log.warn("도감 동기화 잠금 해제 실패. 이미 만료되었습니다. token={}", token);
      return;
    }

    log.debug("도감 동기화 잠금 해제. token={}", token);
  }
}
