-- 도감 설명을 「기본값(설명 파일)」과 「관리자 수정값(override)」으로 분리한다.
-- 기본값은 src/main/resources/catalog/catalog-descriptions.json에서 기동 시와 동기화 직후에 채워지고,
-- 관리자 수정값이 있으면 그 값이, 없으면 기본값이 표시된다(name/feature 등의 xxxOverride와 같은 구조).

-- 1) 기존 child_description은 관리자가 직접 쓰는 값이었으므로 override 컬럼으로 이름을 바꾼다.
--    지금까지는 "아직 작성 안 함"을 빈 문자열로 표현했지만, 이제는 NULL이 "기본값을 따른다"는 뜻이다.
ALTER TABLE catalog_item
    RENAME COLUMN child_description TO child_description_override;

ALTER TABLE catalog_item
    ALTER COLUMN child_description_override DROP NOT NULL;

UPDATE catalog_item
SET child_description_override = NULL
WHERE TRIM(child_description_override) = '';

-- 2) 설명 파일에서 채워지는 기본값 컬럼. 관리자는 이 컬럼을 직접 수정하지 않는다.
ALTER TABLE catalog_item
    ADD COLUMN default_feature TEXT;

ALTER TABLE catalog_item
    ADD COLUMN default_child_description TEXT;
