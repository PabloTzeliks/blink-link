DROP TABLE IF EXISTS url;

CREATE SEQUENCE urls_id_seq
    START WITH 1000000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE urls (
    id BIGINT NOT NULL,
    original_url TEXT NOT NULL,
    short_code VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,

    CONSTRAINT pk_urls PRIMARY KEY (id),
    CONSTRAINT uc_urls_short_code UNIQUE (short_code)
);