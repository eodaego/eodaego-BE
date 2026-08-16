CREATE TABLE crawl_execution_log (
    id               UUID PRIMARY KEY,
    job_type         VARCHAR(255) NOT NULL,   -- enum(STRING): CATALOG_SYNC, FACILITY_IMPORT, EVENT_CRAWL, CATALOG_CRAWL, OPERATING_HOURS_CRAWL, WEATHER_CRAWL, CONGESTION_CRAWL
    success          BOOLEAN NOT NULL,
    collected_count  INTEGER NOT NULL,
    message          VARCHAR(1000) NOT NULL,
    created_at       TIMESTAMP,
    updated_at       TIMESTAMP
);

CREATE INDEX idx_crawl_execution_log_created_at
    ON crawl_execution_log (created_at DESC);
