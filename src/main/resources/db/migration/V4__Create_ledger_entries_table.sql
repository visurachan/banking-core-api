CREATE TABLE ledger_entries (
                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                account_id UUID NOT NULL,
                                transaction_log_id UUID NOT NULL,
                                entry_type VARCHAR(10) NOT NULL,
                                amount NUMERIC(19, 4) NOT NULL,
                                description VARCHAR(255),
                                currency VARCHAR(3) NOT NULL DEFAULT 'GBP',
                                created_at TIMESTAMP NOT NULL DEFAULT NOW(),

                                CONSTRAINT fk_ledger_entries_account
                                    FOREIGN KEY (account_id)
                                        REFERENCES accounts(id),

                                CONSTRAINT fk_ledger_entries_transaction_log
                                    FOREIGN KEY (transaction_log_id)
                                        REFERENCES transaction_logs(id)
);

CREATE INDEX idx_ledger_entries_account_id ON ledger_entries(account_id);
CREATE INDEX idx_ledger_entries_transaction_log_id ON ledger_entries(transaction_log_id);