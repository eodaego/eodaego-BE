package com.chuseok22.eodaegoserver.domain.catalog.service;

import com.chuseok22.eodaegoserver.domain.catalog.CatalogCategory;
import com.chuseok22.eodaegoserver.domain.catalog.dto.response.CatalogItemDetailResponse;
import com.chuseok22.eodaegoserver.domain.catalog.dto.response.CatalogItemListResponse;
import com.chuseok22.eodaegoserver.domain.catalog.dto.response.CatalogItemSummaryResponse;
import com.chuseok22.eodaegoserver.domain.catalog.entity.CatalogItem;
import com.chuseok22.eodaegoserver.domain.catalog.entity.MemberCatalogCollection;
import com.chuseok22.eodaegoserver.domain.catalog.repository.CatalogItemRepository;
import com.chuseok22.eodaegoserver.domain.catalog.repository.MemberCatalogCollectionRepository;
import com.chuseok22.eodaegoserver.domain.member.entity.Member;
import com.chuseok22.eodaegoserver.domain.member.repository.MemberRepository;
import com.chuseok22.eodaegoserver.global.exception.CustomException;
import com.chuseok22.eodaegoserver.global.exception.ErrorCode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogService {

  private final CatalogItemRepository catalogItemRepository;
  private final MemberCatalogCollectionRepository memberCatalogCollectionRepository;
  private final MemberRepository memberRepository;
  private final Clock clock;

  public CatalogItemListResponse getCatalogItems(UUID memberId, CatalogCategory category, String name) {
    List<CatalogItem> catalogItems = findItemsByFilter(memberId, category, name);

    List<UUID> catalogItemsIds = catalogItems.stream()
        .map(CatalogItem::getId)
        .toList();

    Set<UUID> collectedCatalogItemIds = memberCatalogCollectionRepository
        .findByMemberIdAndCatalogItemIdIn(memberId, catalogItemsIds)
        .stream()
        .map(memberCatalogCollection -> memberCatalogCollection.getCatalogItem().getId())
        .collect(Collectors.toSet());

    List<CatalogItemSummaryResponse> items = catalogItems.stream()
        .map(catalogItem -> toSummaryResponse(catalogItem, collectedCatalogItemIds.contains(catalogItem.getId())))
        .toList();

    long collectedCount = category != null
        ? memberCatalogCollectionRepository.countByMemberIdAndCatalogItem_Category(memberId, category)
        : memberCatalogCollectionRepository.countByMemberId(memberId);

    return new CatalogItemListResponse(items.size(), collectedCount, items);
  }

  public CatalogItemDetailResponse getCatalogItemDetail(UUID memberId, UUID catalogItemId) {
    CatalogItem catalogItem = catalogItemRepository.findById(catalogItemId)
        .orElseThrow(() -> {
          log.warn("도감 항목 조회 실패. catalogItemId={}", catalogItemId);
          throw new CustomException(ErrorCode.CATALOG_ITEM_NOT_FOUND);
        });

    MemberCatalogCollection memberCatalogCollection = memberCatalogCollectionRepository
        .findByMemberIdAndCatalogItemId(memberId, catalogItemId)
        .orElseThrow(() -> {
          log.warn("도감 항목 상세 조회 차단. 미수집 항목. memberId={}, catalogItemId={}", memberId, catalogItemId);
          throw new CustomException(ErrorCode.CATALOG_ITEM_NOT_COLLECTED);
        });

    return CatalogItemDetailResponse.from(catalogItem, buildCode(catalogItem), memberCatalogCollection.getCollectedAt());
  }

  @Transactional
  public void collectCatalogItem(UUID memberId, UUID catalogItemId) {
    if (memberCatalogCollectionRepository.findByMemberIdAndCatalogItemId(memberId, catalogItemId).isPresent()) {
      log.warn("도감 항목 수집 실패. 이미 수집한 항목. memberId={}, catalogItemId={}", memberId, catalogItemId);
      throw new CustomException(ErrorCode.CATALOG_ITEM_ALREADY_COLLECTED);
    }

    CatalogItem catalogItem = catalogItemRepository.findById(catalogItemId)
        .orElseThrow(() -> {
          log.warn("도감 항목 수집 실패. 존재하지 않는 항목. catalogItemId={}", catalogItemId);
          throw new CustomException(ErrorCode.CATALOG_ITEM_NOT_FOUND);
        });

    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> {
          log.warn("도감 항목 수집 실패. 존재하지 않는 회원. memberId={}", memberId);
          throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        });

    memberCatalogCollectionRepository.save(MemberCatalogCollection.builder()
        .member(member)
        .catalogItem(catalogItem)
        .collectedAt(LocalDateTime.now(clock))
        .build());

    log.info("도감 항목 수집 완료. memberId={}, catalogItemId={}", memberId, catalogItemId);
  }

  private List<CatalogItem> findItemsByFilter(UUID memberId, CatalogCategory category, String name) {
    if (name != null) {
      return findCollectedItemsByFilter(memberId, category, name);
    } else if (category != null) {
      return catalogItemRepository.findByCategory(category);
    } else {
      return catalogItemRepository.findAll();
    }
  }

  private List<CatalogItem> findCollectedItemsByFilter(UUID memberId, CatalogCategory category, String name) {
    List<MemberCatalogCollection> collections = category != null
        ? memberCatalogCollectionRepository.findByMemberIdAndCatalogItem_CategoryAndCatalogItem_NameContaining(memberId, category, name)
        : memberCatalogCollectionRepository.findByMemberIdAndCatalogItem_NameContaining(memberId, name);

    return collections.stream()
        .map(MemberCatalogCollection::getCatalogItem)
        .toList();
  }

  private CatalogItemSummaryResponse toSummaryResponse(CatalogItem catalogItem, boolean collected) {
    return CatalogItemSummaryResponse.from(catalogItem, buildCode(catalogItem), collected);
  }

  private String buildCode(CatalogItem catalogItem) {
    String prefix = switch (catalogItem.getCategory()) {
      case ANIMAL -> "A";
      case PLANT -> "B";
      case PLACE -> "C";
    };
    return prefix + String.format("%03d", catalogItem.getSequenceNumber());
  }

}
