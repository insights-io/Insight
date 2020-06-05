CREATE SCHEMA IF NOT EXISTS auth;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS auth.organization
(
    id         TEXT        NOT NULL UNIQUE,
    name       TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    PRIMARY KEY (id),
    CONSTRAINT id_length CHECK (length(auth.organization.id) = 6)
);

CREATE TABLE IF NOT EXISTS auth.user
(
    id           UUID        NOT NULL UNIQUE DEFAULT uuid_generate_v4(),
    email        TEXT        NOT NULL UNIQUE,
    org_id       TEXT        NOT NULL,
    role         TEXT        NOT NULL,
    full_name    TEXT        NOT NULL,
    phone_number TEXT,
    created_at   TIMESTAMPTZ NOT NULL        DEFAULT now(),

    PRIMARY KEY (id, email, org_id),
    FOREIGN KEY (org_id) REFERENCES auth.organization (id),
    CONSTRAINT email_length CHECK (length(auth.user.email) < 255)
);

CREATE TABLE IF NOT EXISTS auth.password
(
    user_id    UUID        NOT NULL,
    hash       TEXT        NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    PRIMARY KEY (user_id, hash, created_at),
    FOREIGN KEY (user_id) REFERENCES auth.user (id)
);

CREATE TABLE IF NOT EXISTS auth.password_reset_request
(
    token      UUID        NOT NULL UNIQUE DEFAULT uuid_generate_v4(),
    email      TEXT        NOT NULL,
    user_id    UUID        NOT NULL,
    created_at TIMESTAMPTZ NOT NULL        DEFAULT now(),

    FOREIGN KEY (user_id) REFERENCES auth.user (id),
    FOREIGN KEY (email) REFERENCES auth.user (email),
    PRIMARY KEY (token, email, created_at),
    CONSTRAINT email_length CHECK (length(auth.password_reset_request.email) < 255)
);

CREATE TABLE IF NOT EXISTS auth.sign_up_request
(
    token           UUID        NOT NULL UNIQUE DEFAULT uuid_generate_v4(),
    email           TEXT        NOT NULL UNIQUE,
    hashed_password TEXT        NOT NULL,
    full_name       TEXT        NOT NULL,
    company         TEXT        NOT NULL,
    phone_number    TEXT,
    referer         TEXT,
    created_at      TIMESTAMPTZ NOT NULL        DEFAULT now(),

    PRIMARY KEY (token, email),
    CONSTRAINT email_length CHECK (length(auth.sign_up_request.email) < 255)
);

CREATE TABLE IF NOT EXISTS auth.team_invite
(
    token      UUID        NOT NULL UNIQUE DEFAULT uuid_generate_v4(),
    email      TEXT        NOT NULL,
    creator_id UUID        NOT NULL,
    org_id     TEXT        NOT NULL,
    role       TEXT        NOT NULL,
    created_at TIMESTAMPTZ NOT NULL        DEFAULT now(),

    PRIMARY KEY (token),
    FOREIGN KEY (org_id) REFERENCES auth.organization (id),
    FOREIGN KEY (creator_id) REFERENCES auth.user (id),
    CONSTRAINT no_duplicates UNIQUE (org_id, email),
    CONSTRAINT email_length CHECK (length(auth.team_invite.email) < 255)
);

