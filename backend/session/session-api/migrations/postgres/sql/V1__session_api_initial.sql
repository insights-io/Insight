CREATE SCHEMA IF NOT EXISTS session;

CREATE TABLE IF NOT EXISTS session.session
(
    id              UUID        NOT NULL UNIQUE,
    device_id       UUID        NOT NULL,
    organization_id TEXT        NOT NULL,
    user_agent      JSONB       NOT NULL,
    location        JSONB       NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),

    PRIMARY KEY (id, device_id, organization_id, created_at)
);

CREATE TABLE IF NOT EXISTS session.page_visit
(
    id                 UUID        NOT NULL UNIQUE,
    session_id         UUID        NOT NULL,
    organization_id    TEXT        NOT NULL,
    doctype            TEXT        NOT NULL,
    origin             TEXT        NOT NULL,
    path               TEXT        NOT NULL,
    referrer           TEXT        NOT NULL,
    height             SMALLINT    NOT NULL,
    width              SMALLINT    NOT NULL,
    screen_height      SMALLINT    NOT NULL,
    screen_width       SMALLINT    NOT NULL,
    compiled_timestamp INTEGER     NOT NULL,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    page_end           TIMESTAMPTZ,

    FOREIGN KEY (session_id) REFERENCES session.session (id),
    PRIMARY KEY (id, session_id, organization_id, created_at)
);
