package com.chuseok22.eodaegoserver.domain.admin.repository;

import com.chuseok22.eodaegoserver.domain.admin.CrawlJobType;
import com.chuseok22.eodaegoserver.domain.admin.entity.CrawlExecutionLog;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrawlExecutionLogRepository extends JpaRepository<CrawlExecutionLog, UUID> {

  Optional<CrawlExecutionLog> findTopByJobTypeOrderByCreatedAtDesc(CrawlJobType jobType);
}
