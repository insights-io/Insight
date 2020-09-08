CREATE SCHEMA IF NOT EXISTS auth;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE OR REPLACE FUNCTION updated_at_now()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TABLE IF NOT EXISTS auth.organization
(
    id         TEXT        NOT NULL UNIQUE,
    name       TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    PRIMARY KEY (id),
    CONSTRAINT id_length CHECK (length(auth.organization.id) = 6)
);

CREATE TRIGGER organization_updated_at_now
    BEFORE UPDATE
    ON auth.organization
    FOR EACH ROW
EXECUTE PROCEDURE updated_at_now();

CREATE TABLE IF NOT EXISTS auth.user
(
    id                    UUID        NOT NULL UNIQUE DEFAULT uuid_generate_v4(),
    email                 TEXT        NOT NULL UNIQUE,
    organization_id       TEXT        NOT NULL,
    role                  TEXT        NOT NULL,
    full_name             TEXT        NOT NULL,
    phone_number          JSONB,
    phone_number_verified BOOL        NOT NULL        default false,
    created_at            TIMESTAMPTZ NOT NULL        DEFAULT now(),
    updated_at            TIMESTAMPTZ NOT NULL        DEFAULT now(),

    PRIMARY KEY (id, email, organization_id),
    FOREIGN KEY (organization_id) REFERENCES auth.organization (id),
    CONSTRAINT email_length CHECK (length(auth.user.email) < 255)
);

CREATE TRIGGER user_updated_at_now
    BEFORE UPDATE
    ON auth.user
    FOR EACH ROW
EXECUTE PROCEDURE updated_at_now();

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
    phone_number    JSONB,
    referer         TEXT,
    created_at      TIMESTAMPTZ NOT NULL        DEFAULT now(),

    PRIMARY KEY (token, email),
    CONSTRAINT email_length CHECK (length(auth.sign_up_request.email) < 255)
);

CREATE TABLE IF NOT EXISTS auth.organization_invite
(
    token           UUID        NOT NULL UNIQUE DEFAULT uuid_generate_v4(),
    email           TEXT        NOT NULL,
    creator_id      UUID        NOT NULL,
    organization_id TEXT        NOT NULL,
    role            TEXT        NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL        DEFAULT now(),

    PRIMARY KEY (token),
    FOREIGN KEY (organization_id) REFERENCES auth.organization (id),
    FOREIGN KEY (creator_id) REFERENCES auth.user (id),
    CONSTRAINT no_duplicates UNIQUE (organization_id, email),
    CONSTRAINT email_length CHECK (length(auth.organization_invite.email) < 255)
);

CREATE TABLE auth.user_tfa_setup
(
    user_id    UUID        NOT NULL,
    params     JSONB       NOT NULL,
    method     TEXT        NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    PRIMARY KEY (user_id, method),
    FOREIGN KEY (user_id) REFERENCES auth.user (id)
);

CREATE TABLE auth.organization_sso_setup
(
    organization_id        TEXT        NOT NULL UNIQUE,
    domain                 TEXT        NOT NULL UNIQUE,
    method                 TEXT        NOT NULL,
    configuration_endpoint TEXT        NOT NULL,
    created_at             TIMESTAMPTZ NOT NULL DEFAULT now(),

    PRIMARY KEY (organization_id, domain),
    FOREIGN KEY (organization_id) REFERENCES auth.organization (id)
);

/* Bootstrap Insight organization */
INSERT INTO auth.organization(id, name)
VALUES ('000000', 'Insight');

/* Bootstrap user for Insight organization */
INSERT INTO auth.user(id, email, organization_id, role, full_name)
VALUES ('7c071176-d186-40ac-aaf8-ac9779ab047b', 'admin@insight.io', '000000', 'ADMIN',
        'Admin Admin');
