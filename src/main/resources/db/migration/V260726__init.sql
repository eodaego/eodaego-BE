-- V1__init.sql : 어대GO 초기 스키마 (member, admin, catalog_item, member_catalog_collection, refresh_token)

CREATE TABLE member (
    id                      UUID PRIMARY KEY,
    email                   VARCHAR(255),
    nickname                VARCHAR(255) NOT NULL,
    social_type             VARCHAR(255) NOT NULL,   -- enum(STRING): GOOGLE, APPLE
    provider_id             VARCHAR(255) NOT NULL,
    role                    VARCHAR(255) NOT NULL,   -- enum(STRING): USER, ADMIN
    first_login             BOOLEAN NOT NULL,
    device_id               VARCHAR(255) NOT NULL,
    device_type             VARCHAR(255) NOT NULL,   -- enum(STRING): IOS, ANDROID
    fcm_token               VARCHAR(255),
    privacy_policy_agreed   BOOLEAN NOT NULL,
    location_info_agreed    BOOLEAN NOT NULL,
    terms_of_service_agreed BOOLEAN NOT NULL,
    marketing_agreed        BOOLEAN NOT NULL,
    terms_agreed_at         TIMESTAMP,
    marketing_agreed_at     TIMESTAMP,
    created_at              TIMESTAMP,
    updated_at              TIMESTAMP,
    CONSTRAINT uk_member_social_provider UNIQUE (social_type, provider_id),
    CONSTRAINT uk_member_nickname UNIQUE (nickname)
);

CREATE TABLE admin (
    id         UUID PRIMARY KEY,
    username   VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(255) NOT NULL,   -- enum(STRING): USER, ADMIN
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE catalog_item (
    id                UUID PRIMARY KEY,
    sequence_number   INTEGER NOT NULL,
    category          VARCHAR(255) NOT NULL,   -- enum(STRING): ANIMAL, PLANT, PLACE
    name              VARCHAR(255) NOT NULL,
    feature           VARCHAR(255) NOT NULL,
    child_description VARCHAR(255) NOT NULL,
    status            VARCHAR(255) NOT NULL,   -- enum(STRING): AVAILABLE, SUSPENDED, RETIRED
    image_url         VARCHAR(255),
    latitude          DOUBLE PRECISION,
    longitude         DOUBLE PRECISION,
    external_id       BIGINT,
    created_at        TIMESTAMP,
    updated_at        TIMESTAMP,
    CONSTRAINT uk_catalog_item_category_sequence_number UNIQUE (category, sequence_number),
    CONSTRAINT uk_catalog_item_category_external_id UNIQUE (category, external_id)
);

CREATE TABLE member_catalog_collection (
    id              UUID PRIMARY KEY,
    member_id       UUID NOT NULL,
    catalog_item_id UUID NOT NULL,
    collected_at    TIMESTAMP NOT NULL,
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP,
    CONSTRAINT uk_member_catalog_item UNIQUE (member_id, catalog_item_id),
    CONSTRAINT fk_member_catalog_collection_member
        FOREIGN KEY (member_id) REFERENCES member (id) ON DELETE CASCADE,
    CONSTRAINT fk_member_catalog_collection_catalog_item
        FOREIGN KEY (catalog_item_id) REFERENCES catalog_item (id)
);

CREATE TABLE refresh_token (
    id          UUID PRIMARY KEY,
    member_id   UUID NOT NULL UNIQUE,
    token       VARCHAR(255) NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    CONSTRAINT fk_refresh_token_member
        FOREIGN KEY (member_id) REFERENCES member (id) ON DELETE CASCADE
);
