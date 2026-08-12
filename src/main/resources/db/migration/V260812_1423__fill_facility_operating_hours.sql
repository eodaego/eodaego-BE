-- 시설별 운영시간 초기값을 채운다.
--
-- open_time/close_time/operating_note는 AI 서버가 제공하지 않는 BE 고유 데이터라
-- 동기화(FacilitySyncService.updateFromExternal)가 덮어쓰지 않는다. 여기서 직접 넣는다.
--
-- 원칙: 공원 공식 안내에 명시된 시설에만 시각을 넣는다.
--   공식 안내가 명시하는 것은 두 가지뿐이다.
--     - 정문·후문·구의문·능동문 개방시간 05:00~22:00
--     - 동물원 관람시간 10:00~17:00
--   그 외 시설은 확인된 출처가 없으므로 시각을 NULL로 둔다. 추정값을 노출하면
--   방문객이 헛걸음할 수 있어, 값이 없는 편이 낫다.
--   AI 서버의 운영시간 크롤링(GET /api/v1/facility/operating-hours)도 현재 빈 배열을
--   반환해 원본을 얻을 수 없었다.
--   별도 운영 주체가 있거나 계절제인 시설은 시각 대신 안내 문구만 남긴다.

-- 1) 공식 안내에 개방시간이 명시된 출입문 4곳.
UPDATE facility AS f
SET open_time  = TIME '05:00',
    close_time = TIME '22:00'
WHERE f.source_category = '출입문'
  AND f.name IN ('정문', '후문', '구의문', '능동문');

-- 2) 동물원(동물나라): 공식 관람시간 10:00~17:00.
UPDATE facility AS f
SET open_time      = TIME '10:00',
    close_time     = TIME '17:00',
    operating_note = '방역 상황, 날씨 등에 따라 일부 변동될 수 있습니다.'
WHERE f.source_category = '동물나라';

-- 3) 식물원: 공사로 임시 휴관 중이다. 지도에서는 계속 보여야 한다.
--    마커를 지우면 방문객이 헛걸음하게 된다.
--    도감 수집 차단(catalog_item.status)은 관리자가 직접 처리한다.
UPDATE facility AS f
SET operating_note = '공사로 임시 휴관 중입니다. 재개관 일정은 공원 공지를 확인해 주세요.'
WHERE f.name = '식물원';

-- 4) 별도 운영 주체가 있거나 계절제인 시설: 시각 없이 안내만 남긴다.
UPDATE facility AS f
SET operating_note = '위탁 운영 시설로 운영 시간과 요금이 별도입니다. 현장 안내를 확인해 주세요.'
WHERE f.name IN ('놀이동산', '키즈오토파크');

UPDATE facility AS f
SET operating_note = '별도 기관이 운영하며 관람 시간과 요금이 다릅니다. 서울상상나라 안내를 확인해 주세요.'
WHERE f.name = '서울상상나라';

UPDATE facility AS f
SET operating_note = '여름철에만 운영합니다. 개장 기간은 공원 공지를 확인해 주세요.'
WHERE f.name = '물놀이장';

UPDATE facility AS f
SET operating_note = '매장별로 운영 시간이 다릅니다. 현장 안내를 확인해 주세요.'
WHERE f.source_category = '편의시설'
  AND f.name <> '고객안내센터';
