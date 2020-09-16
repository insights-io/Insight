CREATE SCHEMA IF NOT EXISTS billing;

CREATE TABLE billing.customer
(
    organization_id TEXT        NOT NULL UNIQUE,
    customer_id     TEXT        NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),

    PRIMARY KEY (organization_id, customer_id),
    FOREIGN KEY (organization_id) REFERENCES auth.organization (id)

);

CREATE TABLE billing.subscription
(
    id                  TEXT        NOT NULL,
    organization_id     TEXT        NOT NULL,
    price_id            TEXT        NOT NULL,
    current_period_ends BIGINT      NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),

    PRIMARY KEY (id),
    FOREIGN KEY (organization_id) REFERENCES billing.customer (organization_id)
);