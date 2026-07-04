package com.chuseok22.eodaegoserver.domain.admin.repository;

import com.chuseok22.eodaegoserver.domain.admin.entity.Admin;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, UUID> {

  Optional<Admin> findByUsername(String username);
}
