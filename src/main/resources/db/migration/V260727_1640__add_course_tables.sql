-- V260727_1640__add_course_tables.sql : 코스 추천 도메인 스키마 (course, course_interest_type, course_tag_label, course_place, course_favorite)

CREATE TABLE course (
    id               UUID PRIMARY KEY,
    title            VARCHAR(255),
    duration_minutes INTEGER NOT NULL,
    entrance         VARCHAR(255),   -- enum(STRING): EntranceGate
    exit             VARCHAR(255),   -- enum(STRING): EntranceGate
    created_at       TIMESTAMP,
    updated_at       TIMESTAMP
);

CREATE TABLE course_interest_type (
    course_id       UUID NOT NULL,
    interest_types  VARCHAR(255) NOT NULL,   -- enum(STRING): InterestType
    CONSTRAINT fk_course_interest_type_course
        FOREIGN KEY (course_id) REFERENCES course (id) ON DELETE CASCADE
);

CREATE TABLE course_tag_label (
    course_id  UUID NOT NULL,
    tag_labels VARCHAR(255) NOT NULL,   -- @ElementCollection 값 컬럼(엔티티 필드 tagLabels), 코스당 1~3개
    CONSTRAINT fk_course_tag_label_course
        FOREIGN KEY (course_id) REFERENCES course (id) ON DELETE CASCADE
);

CREATE TABLE course_place (
    id          UUID PRIMARY KEY,
    course_id   UUID NOT NULL,
    visit_order INTEGER NOT NULL,
    facility_id BIGINT,
    name        VARCHAR(255),
    category    VARCHAR(255),   -- enum(STRING): CatalogCategory (ANIMAL, PLANT, PLACE)
    latitude    DOUBLE PRECISION,
    longitude   DOUBLE PRECISION,
    mapx        DOUBLE PRECISION,   -- 엔티티 필드 mapX → Hibernate 네이밍상 mapx (끝자리 단일 대문자엔 언더스코어 미삽입)
    mapy        DOUBLE PRECISION,   -- 엔티티 필드 mapY → mapy
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    CONSTRAINT fk_course_place_course
        FOREIGN KEY (course_id) REFERENCES course (id) ON DELETE CASCADE
);

CREATE TABLE course_favorite (
    id         UUID PRIMARY KEY,
    member_id  UUID NOT NULL,
    course_id  UUID NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uk_course_favorite_member_course UNIQUE (member_id, course_id),
    CONSTRAINT fk_course_favorite_member
        FOREIGN KEY (member_id) REFERENCES member (id) ON DELETE CASCADE,
    CONSTRAINT fk_course_favorite_course
        FOREIGN KEY (course_id) REFERENCES course (id) ON DELETE CASCADE
);
