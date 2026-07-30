-- V260729_2138__rename_course_duration_column.sql : 코스 소요시간 컬럼명을 AI 예상 소요시간 의미에 맞게 변경
-- duration_minutes -> estimated_duration_minutes (요청의 stay_duration_minutes(희망 체류시간)와 구분)

ALTER TABLE course RENAME COLUMN duration_minutes TO estimated_duration_minutes;
