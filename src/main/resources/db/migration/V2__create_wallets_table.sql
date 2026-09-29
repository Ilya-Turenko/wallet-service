CREATE TABLE wallets
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT         NOT NULL UNIQUE,
    balance    NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    currency   VARCHAR(3)     NOT NULL,
    created_at TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_wallet_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
);