package com.chuseok22.eodaegoserver.domain.auth.repository;

import com.chuseok22.eodaegoserver.domain.auth.entity.RefreshToken;
import com.chuseok22.eodaegoserver.domain.member.entity.Member;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

  Optional<RefreshToken> findByMember(Member member);

  void deleteByMember(Member member);
}
