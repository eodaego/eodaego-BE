-- 출입문/고객안내센터 제거로 PLACE의 sequence_number 1번이 비어 도감 코드가 C002부터 시작한다.
-- 회원 수집 기록은 catalog_item_id(UUID)로 연결돼 있어 재채번해도 끊기지 않는다.
-- 유니크 제약 (category, sequence_number) 때문에 한 번에 감산하면 갱신 순서에 따라 충돌할 수
-- 있으므로, 충돌하지 않는 구간(+1000)으로 옮긴 뒤 1부터 다시 매긴다.
UPDATE catalog_item
SET sequence_number = sequence_number + 1000
WHERE category = 'PLACE';

WITH renumbered AS (
  SELECT id, ROW_NUMBER() OVER (ORDER BY sequence_number) AS new_sequence_number
  FROM catalog_item
  WHERE category = 'PLACE'
)
UPDATE catalog_item ci
SET sequence_number = r.new_sequence_number
FROM renumbered r
WHERE ci.id = r.id;
