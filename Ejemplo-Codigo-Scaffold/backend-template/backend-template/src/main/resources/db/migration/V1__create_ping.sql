CREATE TABLE ex_ping (
    id              VARCHAR(64)    NOT NULL,
    idempotency_key VARCHAR(64)    NOT NULL,
    message         VARCHAR(255)   NOT NULL,
    amount          NUMERIC(19, 2) NOT NULL,
    currency        VARCHAR(3)     NOT NULL,
    fee             NUMERIC(19, 2) NOT NULL,
    received_at     TIMESTAMP      NOT NULL,
    version         BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT pk_ex_ping PRIMARY KEY (id),
    CONSTRAINT uk_ex_ping_idempotency_key UNIQUE (idempotency_key)
);
