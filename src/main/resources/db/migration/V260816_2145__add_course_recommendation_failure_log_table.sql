CREATE TABLE course_recommendation_failure_log (
    id             UUID PRIMARY KEY,
    failure_type   VARCHAR(255) NOT NULL,
    message        VARCHAR(1000) NOT NULL,
    created_at     TIMESTAMP,
    updated_at     TIMESTAMP
);

CREATE INDEX idx_course_recommendation_failure_log_created_at ON course_recommendation_failure_log (created_at DESC);
