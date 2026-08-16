package com.chuseok22.eodaegoserver.domain.admin.service;

import com.chuseok22.eodaegoserver.domain.admin.dto.response.MemberDetailView;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.MemberListView;
import com.chuseok22.eodaegoserver.domain.member.SocialType;
import com.chuseok22.eodaegoserver.domain.member.entity.Member;
import com.chuseok22.eodaegoserver.domain.member.repository.MemberRepository;
import com.chuseok22.eodaegoserver.global.exception.CustomException;
import com.chuseok22.eodaegoserver.global.exception.ErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminMemberService {

  private static final int PAGE_SIZE = 20;

  private final MemberRepository memberRepository;

  public Page<MemberListView> searchMembers(String keyword, SocialType socialType, int page) {
    Pageable pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE,
        Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id")));
    Specification<Member> specification = buildSpecification(keyword, socialType);
    log.debug("회원 목록 조회. keyword={}, socialType={}, page={}", keyword, socialType, page);
    return memberRepository.findAll(specification, pageable).map(MemberListView::from);
  }

  public MemberDetailView getMemberDetail(UUID memberId) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    return MemberDetailView.from(member);
  }

  private Specification<Member> buildSpecification(String keyword, SocialType socialType) {
    Specification<Member> specification = Specification.unrestricted();

    if (keyword != null && !keyword.isBlank()) {
      String likePattern = "%" + keyword.toLowerCase() + "%";
      specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder.or(
          criteriaBuilder.like(criteriaBuilder.lower(root.get("nickname")), likePattern),
          criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), likePattern)
      ));
    }

    if (socialType != null) {
      specification = specification.and((root, query, criteriaBuilder) ->
          criteriaBuilder.equal(root.get("socialType"), socialType));
    }

    return specification;
  }
}
