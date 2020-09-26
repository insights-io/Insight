CREATE TABLE billing.subscription_plan
(
    name text PRIMARY KEY
);

INSERT INTO billing.subscription_plan
VALUES ('free'),
       ('business'),
       ('enterprise');

CREATE TABLE billing.customer
(
    external_id TEXT        NOT NULL UNIQUE,
    internal_id TEXT        NOT NULL UNIQUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),

    PRIMARY KEY (external_id, internal_id)
);

CREATE TABLE billing.subscription
(
    id                   TEXT        NOT NULL PRIMARY KEY,
    plan                 TEXT REFERENCES billing.subscription_plan (name) ON UPDATE CASCADE,
    customer_internal_id TEXT REFERENCES billing.customer (internal_id) ON DELETE CASCADE,
    customer_external_id TEXT REFERENCES billing.customer (external_id) ON DELETE CASCADE,
    status               TEXT        NOT NULL,
    price_id             TEXT        NOT NULL,
    current_period_start BIGINT      NOT NULL,
    current_period_end   BIGINT      NOT NULL,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    canceled_at          TIMESTAMPTZ
);

CREATE TABLE billing.invoice
(
    id                   TEXT        NOT NULL PRIMARY KEY,
    subscription_id      TEXT REFERENCES billing.subscription (id) ON DELETE CASCADE,
    customer_internal_id TEXT REFERENCES billing.customer (internal_id) ON DELETE CASCADE,
    customer_external_id TEXT REFERENCES billing.customer (external_id) ON DELETE CASCADE,
    amount_paid          BIGINT      NOT NULL,
    amount_due           BIGINT      NOT NULL,
    status               TEXT        NOT NULL,
    currency             TEXT        NOT NULL,
    payment_intent       TEXT        NOT NULL,
    link                 TEXT        NOT NULL,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now()
);
