-- 출입문 11건(external_id 27~37)과 고객안내센터(external_id 1)는 회원이 수집하는 도감 대상이
-- 아니므로 catalog_item에서 제거한다. 출입문은 EntranceGate enum으로 별도 관리된다.
-- FK(fk_member_catalog_collection_catalog_item) 때문에 수집 기록을 먼저 지운다.
DELETE FROM member_catalog_collection
WHERE catalog_item_id IN (
  SELECT catalog_item_id FROM catalog_item
  WHERE category = 'PLACE' AND external_id IN (1, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37)
);

DELETE FROM catalog_item
WHERE category = 'PLACE' AND external_id IN (1, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37);
