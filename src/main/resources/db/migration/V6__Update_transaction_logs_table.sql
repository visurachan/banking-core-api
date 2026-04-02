
ALTER TABLE transaction_logs
    ALTER COLUMN from_account_id DROP NOT NULL;

ALTER TABLE transaction_logs
    ALTER COLUMN to_account_id DROP NOT NULL;


ALTER TABLE transaction_logs
    ADD COLUMN transaction_type VARCHAR(20) NOT NULL;