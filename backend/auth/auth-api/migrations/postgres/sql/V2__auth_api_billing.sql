CREATE SCHEMA IF NOT EXISTS billing;

CREATE TABLE billing.customer
(
    id              TEXT        NOT NULL UNIQUE,
    organization_id TEXT        NOT NULL UNIQUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),

    PRIMARY KEY (id, organization_id),
    FOREIGN KEY (organization_id) REFERENCES auth.organization (id)
);

CREATE TABLE billing.subscription
(
    id                  TEXT        NOT NULL PRIMARY KEY,
    organization_id     TEXT        NOT NULL,
    price_id            TEXT        NOT NULL,
    current_period_ends BIGINT      NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),

    FOREIGN KEY (organization_id) REFERENCES billing.customer (organization_id)
);

CREATE TABLE billing.invoice
(
    id              TEXT        NOT NULL PRIMARY KEY,
    customer_id     TEXT        NOT NULL,
    subscription_id TEXT        NOT NULL,
    organization_id TEXT        NOT NULL,
    amount_paid     BIGINT      NOT NULL,
    currency        TEXT        NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),

    FOREIGN KEY (organization_id) REFERENCES auth.organization (id),
    FOREIGN KEY (customer_id) REFERENCES billing.customer (id),
    FOREIGN KEY (subscription_id) REFERENCES billing.subscription (id)
);