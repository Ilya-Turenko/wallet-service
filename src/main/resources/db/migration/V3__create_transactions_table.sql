CREATE TABLE transactions
(
    id            BIGSERIAL PRIMARY KEY,
    wallet_id     BIGINT         NOT NULL,
    type          VARCHAR(20)    NOT NULL,
    amount        NUMERIC(19, 2) NOT NULL,
    balance_after NUMERIC(19, 2) NOT NULL,
    created_at    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_transaction_wallet
        FOREIGN KEY (wallet_id)
            REFERENCES wallets (id),

    CONSTRAINT chk_transaction_amount_positive
        CHECK (amount > 0)
);