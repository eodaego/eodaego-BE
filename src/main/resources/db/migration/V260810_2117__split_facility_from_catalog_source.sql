-- 시설(PLACE)을 catalog_source에서 떼어내 facility 테이블로 분리한다.
--
-- 왜: catalog_source는 동물/식물/시설을 한 테이블에 담고 있는데 실제로 쓰는 컬럼이 시설만 다르다.
-- latitude/longitude/intro/facility_type은 시설 전용이라 동물 48건·식물 286건에는 항상 NULL이다.
-- 동물과 식물은 컬럼 구조가 같아 나눌 이유가 없으므로 시설만 분리한다.
-- 시설은 도감뿐 아니라 지도에서도 쓰이고, 운영시간처럼 우리가 직접 관리하는 데이터도 붙는다.
--
-- 분리 후 catalog_item은 source_id(동물·식물) 또는 facility_id(시설) 중 정확히 하나를 참조한다.

-- 1) 시설 테이블. 컬럼을 소유자별로 나눈다.
--
--    [AI 소유] 동기화가 매번 덮어쓴다. AI 서버가 코스 경로 계산·사진 인식 후보 선정에
--    이 값들을 직접 쓰므로 AI가 원본을 가진다.
--      ai_facility_id : AI 서버 Facility.id(PK). AI 응답의 external_id(공공데이터 출처 ID,
--                       관리자 등록 시설은 null)와는 다른 값이므로 이름을 구분한다.
--      code           : 출입문의 MAIN_GATE 등 안정적 영문 식별자. 코스 추천의
--                       entrance_facility_code와 매칭된다. 회원 응답에는 노출하지 않는다.
--      source_category: AI 원본 분류("편의시설", "출입문"). 도감 분류인
--                       catalog_item.category(ANIMAL/PLANT/PLACE)와는 다른 값이다.
--
--    [BE 소유] 동기화가 절대 건드리지 않는다. AI 서버는 시설별 운영시간을 아예 제공하지 않고
--    (공원 전체 기준 HTML 조각만 크롤링한다) 어떤 AI 기능도 운영시간을 읽지 않으므로,
--    앱이 "지금 운영중"을 판단할 수 있도록 우리가 구조화해서 직접 관리한다.
CREATE TABLE facility (
    id              UUID PRIMARY KEY,

    ai_facility_id  BIGINT           NOT NULL UNIQUE,
    code            VARCHAR(50),
    source_category VARCHAR(255),
    name            VARCHAR(255)     NOT NULL,
    intro           TEXT,
    description     TEXT,
    latitude        DOUBLE PRECISION,
    longitude       DOUBLE PRECISION,
    facility_type   VARCHAR(255),
    -- AI가 이 시설을 마지막으로 보내준 시각. 값이 바뀌지 않아도 매 동기화마다 갱신한다.
    -- updated_at(값이 바뀐 시각)과 구분되며, AI 응답에서 사라진 시설을 찾는 근거가 된다.
    last_seen_at    TIMESTAMP        NOT NULL,

    -- null이면 상시 개방이거나 아직 정보를 채우지 않은 상태다.
    open_time       TIME,
    close_time      TIME,
    -- 시간으로 표현할 수 없는 예외 안내("동절기 17:00까지", "매주 월요일 휴관").
    operating_note  VARCHAR(255),

    created_at      TIMESTAMP,
    updated_at      TIMESTAMP
);

-- 2) catalog_source의 PLACE 행을 facility로 이관한다.
--    id를 그대로 물려받는다 — 그래야 3)에서 catalog_item.source_id 값을 facility_id로
--    옮기는 것만으로 연결이 유지된다.
--    code/source_category는 기존 테이블에 없던 컬럼이라 NULL로 두고 다음 동기화가 채운다.
INSERT INTO facility (
    id, ai_facility_id, name, intro, description,
    latitude, longitude, facility_type, last_seen_at, created_at, updated_at
)
SELECT cs.id, cs.external_id, cs.name, cs.intro, cs.description,
       cs.latitude, cs.longitude, cs.facility_type, cs.last_seen_at, cs.created_at, cs.updated_at
FROM catalog_source AS cs
WHERE cs.category = 'PLACE';

-- 3) catalog_item의 참조를 갈아끼운다.
--    NOT NULL을 먼저 풀어야 아래 UPDATE에서 source_id에 NULL을 넣을 수 있다.
ALTER TABLE catalog_item ADD COLUMN facility_id UUID;
ALTER TABLE catalog_item ALTER COLUMN source_id DROP NOT NULL;

-- SET의 우변은 갱신 전 행 값으로 계산되므로 facility_id에 옛 source_id가 그대로 들어간다.
-- 별칭(ci)을 붙여 어느 테이블의 컬럼인지 명시한다.
UPDATE catalog_item AS ci
SET facility_id = ci.source_id,
    source_id   = NULL
WHERE ci.category = 'PLACE';

-- 도감 항목 1건은 원본 1건과 1:1로 대응한다.
-- PostgreSQL의 UNIQUE는 NULL을 중복으로 보지 않으므로 나머지 행이 NULL이어도 문제없다.
ALTER TABLE catalog_item ADD CONSTRAINT uk_catalog_item_facility_id UNIQUE (facility_id);
ALTER TABLE catalog_item ADD CONSTRAINT fk_catalog_item_facility
    FOREIGN KEY (facility_id) REFERENCES facility (id);

-- 둘 다 NULL이거나 둘 다 채워진 행이 생기지 않도록 DB 레벨에서 막는다.
ALTER TABLE catalog_item ADD CONSTRAINT ck_catalog_item_single_source CHECK (
    (source_id IS NOT NULL)::int + (facility_id IS NOT NULL)::int = 1
);

-- 4) catalog_source에서 시설 흔적을 지운다.
--    3)에서 참조를 이미 끊었으므로 FK 위반 없이 삭제된다.
DELETE FROM catalog_source AS cs WHERE cs.category = 'PLACE';

--    시설 전용 컬럼을 제거한다. 컬럼 자체를 없애야 동기화 코드가 시설 값을 여기에 쓰는 것이
--    물리적으로 불가능해진다.
ALTER TABLE catalog_source DROP COLUMN intro;
ALTER TABLE catalog_source DROP COLUMN latitude;
ALTER TABLE catalog_source DROP COLUMN longitude;
ALTER TABLE catalog_source DROP COLUMN facility_type;
