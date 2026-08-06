-- 도감을 「AI 원본(catalog_source)」과 「우리 콘텐츠(catalog_item)」로 분리한다.
-- 두 데이터가 한 테이블에 섞여 있어, 재동기화가 관리자 수정값을 덮어쓰는 것을 구조적으로 막을 수 없었다.
-- 분리 후 동기화는 catalog_source만, 관리자는 catalog_item만 쓴다.

-- 1) AI 원본 테이블. 동기화 코드만 여기에 쓴다.
--    intro/facility_type은 지금까지 버려지던 AI 필드로, 앞으로를 위해 함께 보관한다.
--    description은 AI 설명이 255자를 넘겨 저장에 실패한 이력이 있어 처음부터 TEXT로 둔다.
CREATE TABLE catalog_source (
    id            UUID PRIMARY KEY,
    category      VARCHAR(255)     NOT NULL,   -- enum(STRING): ANIMAL, PLANT, PLACE
    external_id   BIGINT           NOT NULL,
    name          VARCHAR(255)     NOT NULL,
    image_url     VARCHAR(255),
    latitude      DOUBLE PRECISION,
    longitude     DOUBLE PRECISION,
    description   TEXT,                        -- catalog_item.feature의 원본
    intro         TEXT,
    facility_type VARCHAR(255),
    -- AI가 이 항목을 마지막으로 보내준 시각. 값이 바뀌지 않아도 매 동기화마다 갱신한다.
    -- updated_at(값이 바뀐 시각)과 구분되며, AI 응답에서 사라진 항목을 찾아내는 근거가 된다.
    last_seen_at  TIMESTAMP        NOT NULL,
    created_at    TIMESTAMP,
    updated_at    TIMESTAMP,
    -- AI 서버는 동물/식물/시설마다 id를 각자 1부터 매기므로 external_id 단독으로는 유일하지 않다.
    CONSTRAINT uk_catalog_source_category_external_id UNIQUE (category, external_id)
);

-- 2) 기존 도감 데이터를 AI 원본으로 이관한다.
--    지금까지 동기화가 name/image_url/좌표를 계속 덮어써 왔으므로 현재값 == AI값이 보장된다.
--    feature도 ANIMAL은 전부 빈 값, PLANT/PLACE는 전부 AI description에서 온 값임을 확인했다.
--    created_at/updated_at은 기존 값을 그대로 물려받아 "언제 들어온 항목인지"를 보존한다.
--    last_seen_at은 과거 이력이 없어 복원할 수 없으므로 이관 시점을 기준으로 삼는다.
INSERT INTO catalog_source (
    id, category, external_id, name, image_url, latitude, longitude, description, last_seen_at, created_at, updated_at
)
SELECT
    gen_random_uuid(), category, external_id, name, image_url, latitude, longitude,
    NULLIF(feature, ''), now(), created_at, updated_at
FROM catalog_item;

-- 3) catalog_item에 연결 컬럼과 override 컬럼을 추가한다.
--    override가 null이면 AI 값을 따르고, 값이 있으면 그 값을 쓴다.
ALTER TABLE catalog_item ADD COLUMN source_id          UUID;
ALTER TABLE catalog_item ADD COLUMN name_override      VARCHAR(255);
ALTER TABLE catalog_item ADD COLUMN image_url_override VARCHAR(255);
ALTER TABLE catalog_item ADD COLUMN feature_override   TEXT;
ALTER TABLE catalog_item ADD COLUMN latitude_override  DOUBLE PRECISION;
ALTER TABLE catalog_item ADD COLUMN longitude_override DOUBLE PRECISION;

-- 4) 방금 만든 원본 행과 연결한다.
--    반드시 별칭(ci/cs)을 붙여 어느 테이블의 컬럼인지 명시한다.
UPDATE catalog_item AS ci
SET source_id = cs.id
FROM catalog_source AS cs
WHERE cs.category = ci.category
  AND cs.external_id = ci.external_id;

-- 5) 연결을 강제한다. 도감 항목 1건은 AI 원본 1건과 1:1로 대응한다.
ALTER TABLE catalog_item ALTER COLUMN source_id SET NOT NULL;
ALTER TABLE catalog_item ADD CONSTRAINT uk_catalog_item_source_id UNIQUE (source_id);
ALTER TABLE catalog_item ADD CONSTRAINT fk_catalog_item_source
    FOREIGN KEY (source_id) REFERENCES catalog_source (id);

-- 6) AI 소유로 넘어간 컬럼을 catalog_item에서 제거한다.
--    컬럼 자체를 없애야 동기화 코드가 우리 콘텐츠를 덮어쓰는 것이 물리적으로 불가능해진다.
--    category/sequence_number/status/child_description은 우리 소유이므로 그대로 둔다.
ALTER TABLE catalog_item DROP CONSTRAINT uk_catalog_item_category_external_id;
ALTER TABLE catalog_item DROP COLUMN external_id;
ALTER TABLE catalog_item DROP COLUMN name;
ALTER TABLE catalog_item DROP COLUMN image_url;
ALTER TABLE catalog_item DROP COLUMN latitude;
ALTER TABLE catalog_item DROP COLUMN longitude;
ALTER TABLE catalog_item DROP COLUMN feature;
