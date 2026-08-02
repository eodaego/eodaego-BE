-- V260727_1745__add_course_child_fk_indexes.sql
-- 코스 하위 테이블의 course_id FK 인덱스 추가.
-- PostgreSQL은 FK 컬럼에 인덱스를 자동 생성하지 않아, 컬렉션 배치 조회(course_id IN (...))와
-- 코스 삭제 시 ON DELETE CASCADE가 인덱스 없이 seq scan으로 동작하는 것을 방지한다.
-- course_favorite는 uk_course_favorite_member_course(member_id, course_id) 유니크 인덱스가
-- member_id 선두라 course_id 단독 조회를 지원하지 못하므로 별도 인덱스를 추가한다.

CREATE INDEX idx_course_interest_type_course_id ON course_interest_type (course_id);
CREATE INDEX idx_course_tag_label_course_id ON course_tag_label (course_id);
CREATE INDEX idx_course_place_course_id ON course_place (course_id);
CREATE INDEX idx_course_favorite_course_id ON course_favorite (course_id);
