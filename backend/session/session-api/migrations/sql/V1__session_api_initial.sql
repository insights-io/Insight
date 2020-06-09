CREATE SCHEMA IF NOT EXISTS session;

CREATE TABLE IF NOT EXISTS session.page
(
    id                 UUID        NOT NULL,
    device_id          UUID        NOT NULL,
    session_id         UUID        NOT NULL,
    organization_id    TEXT        NOT NULL,
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

    PRIMARY KEY (id, device_id, session_id, organization_id, page_start)
);
