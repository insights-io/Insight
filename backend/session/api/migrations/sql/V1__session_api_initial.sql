CREATE EXTENSION IF NOT EXISTS "uuid-ossp" SCHEMA public;

CREATE SCHEMA IF NOT EXISTS rec;

CREATE TABLE IF NOT EXISTS rec.page
(
    id                 UUID        NOT NULL,
    uid                UUID        NOT NULL,
    session_id         UUID        NOT NULL,
    org_id             TEXT        NOT NULL,
    doctype            TEXT        NOT NULL,
    url                TEXT        NOT NULL,
    referrer           TEXT        NOT NULL,
    height             SMALLINT    NOT NULL,
    width              SMALLINT    NOT NULL,
    screen_height      SMALLINT    NOT NULL,
    screen_width       SMALLINT    NOT NULL,
    compiled_timestamp INTEGER     NOT NULL,
    page_start         TIMESTAMPTZ NOT NULL DEFAULT now(),
    page_end           TIMESTAMPTZ,

    PRIMARY KEY (id, uid, session_id, org_id, page_start)
);
