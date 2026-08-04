-- 출입문 11건(external_id 27~37)과 고객안내센터(external_id 1)는 회원이 수집하는 도감 대상이
-- 아니므로 catalog_item에서 제거한다. 출입문은 EntranceGate enum으로 별도 관리된다.
-- FK(fk_member_catalog_collection_catalog_item) 때문에 수집 기록을 먼저 지운다.
-- 서브쿼리에서 catalog_item의 PK는 반드시 별칭을 붙인 ci.id로 참조한다. catalog_item에는
-- catalog_item_id 컬럼이 없어, 그 이름을 쓰면 바깥 테이블의 컬럼을 참조하는 상관 서브쿼리가 되어
-- member_catalog_collection 전체가 삭제된다.
DELETE FROM member_catalog_collection
WHERE catalog_item_id IN (
  SELECT ci.id
  FROM catalog_item AS ci
  WHERE ci.category = 'PLACE'
    AND ci.external_id IN (1, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37)
);

DELETE FROM catalog_item
WHERE category = 'PLACE' AND external_id IN (1, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37);
