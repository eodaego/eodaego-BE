package com.chuseok22.eodaegoserver.domain.admin.service;

import com.chuseok22.eodaegoserver.domain.admin.dto.request.MemberListQuery;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.MemberDetailView;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.MemberListView;
import com.chuseok22.eodaegoserver.domain.member.entity.Member;
import com.chuseok22.eodaegoserver.domain.member.repository.MemberRepository;
import com.chuseok22.eodaegoserver.global.exception.CustomException;
import com.chuseok22.eodaegoserver.global.exception.ErrorCode;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminMemberService {

  private static final int PAGE_SIZE = 20;
  private static final String DEFAULT_SORT_PROPERTY = "createdAt";
  private static final Set<String> SORTABLE_PROPERTIES = Set.of("createdAt", "nickname", "socialType", "deviceType");
  private static final List<String> CSV_HEADER = List.of("닉네임", "이메일", "소셜타입", "기기타입", "가입일");
  private static final DateTimeFormatter CREATED_AT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  private final MemberRepository memberRepository;

  public Page<MemberListView> searchMembers(MemberListQuery query) {
    Specification<Member> specification = buildSpecification(query);
    Sort sort = buildSort(query);
    int page = Math.max(query.page(), 0);
    Page<Member> result = memberRepository.findAll(specification, PageRequest.of(page, PAGE_SIZE, sort));

    // 범위를 벗어난 페이지 요청이 빈 화면이 되지 않도록 마지막 페이지로 보정한다.
    if (page > 0 && result.getTotalPages() > 0 && page >= result.getTotalPages()) {
      result = memberRepository.findAll(specification, PageRequest.of(result.getTotalPages() - 1, PAGE_SIZE, sort));
    }

    log.debug("회원 목록 조회. hasKeyword={}, socialType={}, deviceType={}, sort={}, page={}",
        query.keyword() != null && !query.keyword().isBlank(), query.socialType(), query.deviceType(),
        query.sort(), query.page());
    return result.map(MemberListView::from);
  }

  // 회원 수가 CSV로 한 번에 내려받을 만큼 작다고 가정하고 조건에 맞는 전체 결과를 메모리에 올린다.
  public byte[] exportMembersCsv(MemberListQuery query) {
    List<List<String>> rows = memberRepository.findAll(buildSpecification(query), buildSort(query)).stream()
        .map(member -> List.of(
            member.getNickname(),
            Objects.requireNonNullElse(member.getEmail(), ""),
            member.getSocialType().name(),
            member.getDeviceType().name(),
            CREATED_AT_FORMATTER.format(member.getCreatedAt())))
        .toList();
    log.info("관리자 회원 CSV 내보내기. {}건, socialType={}, deviceType={}, hasKeyword={}",
        rows.size(), query.socialType(), query.deviceType(), query.keyword() != null && !query.keyword().isBlank());
    return AdminCsvWriter.write(CSV_HEADER, rows);
  }

  public MemberDetailView getMemberDetail(UUID memberId) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    return MemberDetailView.from(member);
  }

  // 허용되지 않은 정렬 키·방향은 기본 정렬(가입일 내림차순)로 되돌려 임의 컬럼 정렬 시도가 예외가 되지 않게 한다.
  private Sort buildSort(MemberListQuery query) {
    // Set.of는 contains(null)에서 NPE를 던지므로 sort 파라미터가 없는 기본 요청을 먼저 걸러낸다.
    String sortProperty = query.sort() != null && SORTABLE_PROPERTIES.contains(query.sort())
        ? query.sort() : DEFAULT_SORT_PROPERTY;
    Sort.Direction defaultDirection =
        DEFAULT_SORT_PROPERTY.equals(sortProperty) ? Sort.Direction.DESC : Sort.Direction.ASC;
    Sort.Direction direction = "asc".equals(query.direction()) ? Sort.Direction.ASC
        : "desc".equals(query.direction()) ? Sort.Direction.DESC : defaultDirection;
    return Sort.by(direction, sortProperty).and(Sort.by(Sort.Direction.DESC, "id"));
  }

  private Specification<Member> buildSpecification(MemberListQuery query) {
    Specification<Member> specification = Specification.unrestricted();

    if (query.keyword() != null && !query.keyword().isBlank()) {
      String likePattern = "%" + query.keyword().toLowerCase() + "%";
      specification = specification.and((root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.or(
          criteriaBuilder.like(criteriaBuilder.lower(root.get("nickname")), likePattern),
          criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), likePattern)
      ));
    }

    if (query.socialType() != null) {
      specification = specification.and((root, criteriaQuery, criteriaBuilder) ->
          criteriaBuilder.equal(root.get("socialType"), query.socialType()));
    }

    if (query.deviceType() != null) {
      specification = specification.and((root, criteriaQuery, criteriaBuilder) ->
          criteriaBuilder.equal(root.get("deviceType"), query.deviceType()));
    }

    return specification;
  }
}
