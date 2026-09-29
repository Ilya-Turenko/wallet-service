ALTER TABLE transactions
    ADD COLUMN related_wallet_id BIGINT,
    ADD COLUMN transfer_id UUID;

ALTER TABLE transactions
    ADD CONSTRAINT fk_transaction_related_wallet
        FOREIGN KEY (related_wallet_id)
            REFERENCES wallets (id);

CREATE INDEX idx_transactions_transfer_id
    ON transactions (transfer_id);