package com.chuseok22.eodaegoserver.domain.member.repository;

import com.chuseok22.eodaegoserver.domain.member.SocialType;
import com.chuseok22.eodaegoserver.domain.member.entity.Member;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, UUID> {

  Optional<Member> findBySocialTypeAndProviderId(SocialType socialType, String providerId);
}
