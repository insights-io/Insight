CREATE EXTENSION "uuid-ossp";

CREATE FUNCTION updated_at_now()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TABLE auth.user_role
(
    name text PRIMARY KEY
);

INSERT INTO auth.user_role
VALUES ('member'),
       ('admin'),
       ('owner');

CREATE TABLE auth.organization
(
    id              TEXT PRIMARY KEY,
    name            TEXT,
    open_membership BOOL                                                    DEFAULT FALSE,
    enforce_multi_factor_authentication     BOOL                            DEFAULT FALSE,
    default_role    TEXT REFERENCES auth.user_role (name) ON UPDATE CASCADE DEFAULT 'member',
    avatar          JSONB,
    created_at      TIMESTAMPTZ NOT NULL                                    DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL                                    DEFAULT now(),

    CONSTRAINT id_length CHECK (length(auth.organization.id) = 6)
);

CREATE TRIGGER organization_updated_at_now
    BEFORE UPDATE
    ON auth.organization
    FOR EACH ROW
EXECUTE PROCEDURE updated_at_now();

CREATE TABLE auth.user
(
    id                    UUID        NOT NULL UNIQUE DEFAULT uuid_generate_v4(),
    email                 TEXT        NOT NULL UNIQUE,
    organization_id       TEXT REFERENCES auth.organization (id) ON DELETE CASCADE,
    role                  TEXT REFERENCES auth.user_role (name) ON UPDATE CASCADE,
    full_name             TEXT        NOT NULL,
    phone_number          JSONB,
    phone_number_verified BOOL        NOT NULL        default false,
    created_at            TIMESTAMPTZ NOT NULL        DEFAULT now(),
    updated_at            TIMESTAMPTZ NOT NULL        DEFAULT now(),

    PRIMARY KEY (id, email, organization_id),
    CONSTRAINT email_length CHECK (length(auth.user.email) < 255)
);

CREATE TRIGGER user_updated_at_now
    BEFORE UPDATE
    ON auth.user
    FOR EACH ROW
EXECUTE PROCEDURE updated_at_now();

CREATE TABLE IF NOT EXISTS auth.organization_team_invite
(
    token           UUID PRIMARY KEY     DEFAULT uuid_generate_v4(),
    email           TEXT        NOT NULL,
    creator_id      UUID REFERENCES auth.user (id) ON DELETE CASCADE,
    role            TEXT REFERENCES auth.user_role (name) ON UPDATE CASCADE,
    organization_id TEXT REFERENCES auth.organization (id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT email_length CHECK (length(auth.organization_team_invite.email) < 255)
);

CREATE TABLE auth.password
(
    user_id    UUID REFERENCES auth.user (id) ON DELETE CASCADE,
    hash       TEXT        NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    PRIMARY KEY (user_id, hash, created_at)
);

CREATE TABLE auth.password_reset_request
(
    token      UUID        NOT NULL UNIQUE DEFAULT uuid_generate_v4(),
    email      TEXT REFERENCES auth.user (email) ON DELETE CASCADE,
    user_id    UUID REFERENCES auth.user (id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL        DEFAULT now(),

    PRIMARY KEY (token, email, created_at),
    CONSTRAINT email_length CHECK (length(auth.password_reset_request.email) < 255)
);

CREATE TABLE auth.sign_up_request
(
    token           UUID        NOT NULL UNIQUE DEFAULT uuid_generate_v4(),
    email           TEXT        NOT NULL UNIQUE,
    hashed_password TEXT        NOT NULL,
    full_name       TEXT        NOT NULL,
    company         TEXT        NOT NULL,
    phone_number    JSONB,
    referrer        TEXT,
    created_at      TIMESTAMPTZ NOT NULL        DEFAULT now(),

    PRIMARY KEY (token, email),
    CONSTRAINT email_length CHECK (length(auth.sign_up_request.email) < 255)
);

CREATE TABLE auth.user_mfa_configuration
(
    user_id    UUID REFERENCES auth.user (id) ON DELETE CASCADE,
    params     JSONB       NOT NULL,
    method     TEXT        NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    PRIMARY KEY (user_id, method)
);

CREATE TABLE auth.sso_method
(
    name text PRIMARY KEY
);

INSERT INTO auth.sso_method
VALUES ('saml'),
       ('google'),
       ('microsoft'),
       ('github');

CREATE TABLE auth.organization_sso_setup
(
    organization_id        TEXT REFERENCES auth.organization (id) ON DELETE CASCADE,
    domain                 TEXT        NOT NULL UNIQUE,
    method                 TEXT REFERENCES auth.sso_method (name) ON UPDATE CASCADE,
    saml                   JSONB,
    created_at             TIMESTAMPTZ NOT NULL DEFAULT now(),

    PRIMARY KEY (organization_id, domain)
);

CREATE TABLE auth.organization_password_policy
(
    organization_id                    TEXT REFERENCES auth.organization (id) ON DELETE CASCADE UNIQUE,
    min_characters                     SMALLINT    NOT NULL DEFAULT 8,
    prevent_password_reuse             BOOL        NOT NULL DEFAULT TRUE,
    require_uppercase_character        BOOL        NOT NULL DEFAULT FALSE,
    require_lowercase_character        BOOL        NOT NULL DEFAULT FALSE,
    require_number                     BOOl        NOT NULL DEFAULT FALSE,
    require_non_alphanumeric_character BOOL        NOT NULL DEFAULT FALSE,
    created_at                         TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at                         TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TRIGGER organization_password_policy_updated_at_now
    BEFORE UPDATE
    ON auth.organization_password_policy
    FOR EACH ROW
EXECUTE PROCEDURE updated_at_now();

CREATE TABLE auth.token
(
    token      TEXT        NOT NULL,
    user_id    UUID REFERENCES auth.user (id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    PRIMARY KEY (token, user_id)
);


/* Bootstrap Rebrowse organization */
INSERT INTO auth.organization(id, name)
VALUES ('000000', 'Rebrowse');

/* Bootstrap genesis user for Rebrowse organization */
INSERT INTO auth.user(id, email, organization_id, role, full_name)
VALUES ('7c071176-d186-40ac-aaf8-ac9779ab047b', 'admin@rebrowse.dev', '000000', 'admin',
        'Admin Admin');
