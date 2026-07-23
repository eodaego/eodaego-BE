package com.chuseok22.eodaegoserver.domain.member.service;

import com.chuseok22.eodaegoserver.domain.member.dto.request.AgreementRequest;
import com.chuseok22.eodaegoserver.domain.member.dto.request.NicknameUpdateRequest;
import com.chuseok22.eodaegoserver.domain.member.dto.response.AgreementResponse;
import com.chuseok22.eodaegoserver.domain.member.dto.response.NicknameResponse;
import com.chuseok22.eodaegoserver.domain.member.entity.Member;
import com.chuseok22.eodaegoserver.domain.member.repository.MemberRepository;
import com.chuseok22.eodaegoserver.global.exception.CustomException;
import com.chuseok22.eodaegoserver.global.exception.ErrorCode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

  private final Clock clock;
  private final MemberRepository memberRepository;

  public AgreementResponse getAgreement(UUID memberId) {
    Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    return AgreementResponse.from(member);
  }

  @Transactional
  public void updateAgreement(UUID memberId, AgreementRequest request) {
    Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    member.setPrivacyPolicyAgreed(request.privacyPolicyAgreed());
    member.setLocationInfoAgreed(request.locationInfoAgreed());
    member.setTermsOfServiceAgreed(request.termsOfServiceAgreed());
    if (member.getTermsAgreedAt() == null) {
      member.setTermsAgreedAt(LocalDateTime.now(clock));
    }

    boolean marketingAgreed = request.marketingAgreed();

    member.setMarketingAgreed(marketingAgreed);
    if (!marketingAgreed) {
      member.setMarketingAgreedAt(null);
    } else if (member.getMarketingAgreedAt() == null) {
      member.setMarketingAgreedAt(LocalDateTime.now(clock));
    }

    log.info("약관 동의 정보 수정: memberId={}", memberId);
  }

  @Transactional
  public NicknameResponse updateNickname(UUID memberId, NicknameUpdateRequest request) {
    Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    String nickname = request.nickname();

    member.setNickname(nickname);

    try {
      memberRepository.flush();
    } catch (DataIntegrityViolationException e) {
      throw new CustomException(ErrorCode.NICKNAME_ALREADY_EXISTS);
    }

    log.info("닉네임 변경 완료: memberId={}, nickname={}", memberId, nickname);

    return new NicknameResponse(nickname);
  }

  @Transactional
  public void withdraw(UUID memberId) {
    Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    memberRepository.delete(member);

    log.info("회원탈퇴 완료: memberId={}", memberId);
  }
}
