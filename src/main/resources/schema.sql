CREATE TABLE fare
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    base_figure  INT                   NOT NULL,
    final_figure INT                   NOT NULL,
    stopover_id  BIGINT                NULL,
    CONSTRAINT pk_fare PRIMARY KEY (id)
);

CREATE TABLE location
(
    id        BIGINT AUTO_INCREMENT NOT NULL,
    address   VARCHAR(255)          NOT NULL,
    latitude  DOUBLE                NOT NULL,
    longitude DOUBLE                NOT NULL,
    CONSTRAINT pk_location PRIMARY KEY (id)
);

CREATE TABLE member
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    name          VARCHAR(10)           NOT NULL,
    email         VARCHAR(255)          NOT NULL,
    phone         VARCHAR(30)           NOT NULL,
    gender        VARCHAR(255)          NOT NULL,
    profile_image VARCHAR(1500)         NULL,
    CONSTRAINT pk_member PRIMARY KEY (id)
);

CREATE TABLE party
(
    id             BIGINT AUTO_INCREMENT NOT NULL,
    max_person     INT                   NOT NULL,
    radius         DOUBLE                NOT NULL,
    departure_time datetime              NOT NULL,
    gender_option  VARCHAR(255)          NOT NULL,
    CONSTRAINT pk_party PRIMARY KEY (id)
);

CREATE TABLE party_member
(
    id        BIGINT AUTO_INCREMENT NOT NULL,
    member_id BIGINT                NULL,
    party_id  BIGINT                NULL,
    `role`    VARCHAR(255)          NOT NULL,
    CONSTRAINT pk_party_member PRIMARY KEY (id)
);

CREATE TABLE payment_status
(
    id              BIGINT AUTO_INCREMENT NOT NULL,
    party_member_id BIGINT                NOT NULL,
    stopover_id     BIGINT                NOT NULL,
    is_paid         BIT(1)                NOT NULL,
    CONSTRAINT pk_payment_status PRIMARY KEY (id)
);

CREATE TABLE review
(
    id        BIGINT AUTO_INCREMENT NOT NULL,
    author    VARCHAR(10)           NOT NULL,
    score     DOUBLE                NOT NULL,
    comment   VARCHAR(1000)         NOT NULL,
    member_id BIGINT                NULL,
    CONSTRAINT pk_review PRIMARY KEY (id)
);

CREATE TABLE stopover
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    location_id   BIGINT                NULL,
    party_id      BIGINT                NULL,
    fare_id       BIGINT                NULL,
    stopover_type VARCHAR(255)          NOT NULL,
    CONSTRAINT pk_stopover PRIMARY KEY (id)
);

ALTER TABLE member
    ADD CONSTRAINT uc_member_email UNIQUE (email);

ALTER TABLE fare
    ADD CONSTRAINT FK_FARE_ON_STOPOVER FOREIGN KEY (stopover_id) REFERENCES stopover (id);

ALTER TABLE party_member
    ADD CONSTRAINT FK_PARTY_MEMBER_ON_MEMBER FOREIGN KEY (member_id) REFERENCES member (id);

ALTER TABLE party_member
    ADD CONSTRAINT FK_PARTY_MEMBER_ON_PARTY FOREIGN KEY (party_id) REFERENCES party (id);

ALTER TABLE payment_status
    ADD CONSTRAINT FK_PAYMENT_STATUS_ON_PARTY_MEMBER FOREIGN KEY (party_member_id) REFERENCES party_member (id);

ALTER TABLE payment_status
    ADD CONSTRAINT FK_PAYMENT_STATUS_ON_STOPOVER FOREIGN KEY (stopover_id) REFERENCES stopover (id);

ALTER TABLE review
    ADD CONSTRAINT FK_REVIEW_ON_MEMBER FOREIGN KEY (member_id) REFERENCES member (id);

ALTER TABLE stopover
    ADD CONSTRAINT FK_STOPOVER_ON_FARE FOREIGN KEY (fare_id) REFERENCES fare (id);

ALTER TABLE stopover
    ADD CONSTRAINT FK_STOPOVER_ON_LOCATION FOREIGN KEY (location_id) REFERENCES location (id);

ALTER TABLE stopover
    ADD CONSTRAINT FK_STOPOVER_ON_PARTY FOREIGN KEY (party_id) REFERENCES party (id);

CREATE INDEX IDX_STOPOVER_ON_PARTY ON stopover(party_id);
CREATE INDEX IDX_STOPOVER_ON_LOCATION ON stopover(location_id);
CREATE INDEX IDX_FARE_ON_STOPOVER ON fare(stopover_id);
CREATE INDEX IDX_PARTY_MEMBER_ON_MEMBER ON party_member(member_id);
CREATE INDEX IDX_PARTY_ON_MEMBER_PARTY ON party_member(party_id);
CREATE INDEX IDX_FARE_ON_STOPOVER ON fare(stopover_id);
CREATE INDEX IDX_PAYMENT_STATUS_ON_PARTY_MEMBER ON payment_status(party_member_id);
CREATE INDEX IDX_PAYMENT_STATUS_ON_STOPOVER ON payment_status(stopover_id);