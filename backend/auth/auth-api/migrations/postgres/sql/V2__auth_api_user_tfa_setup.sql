CREATE TABLE auth.user_tfa_setup
(
    user_id    UUID        NOT NULL UNIQUE,
    secret     TEXT        NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    PRIMARY KEY (user_id),
    FOREIGN KEY (user_id) REFERENCES auth.user (id)
);
