CREATE TABLE transaction_logs (
                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  reference VARCHAR(255) UNIQUE NOT NULL,
                                  amount NUMERIC(19, 4) NOT NULL,
                                  status VARCHAR(20) NOT NULL,
                                  currency VARCHAR(3) NOT NULL DEFAULT 'GBP',
                                  description VARCHAR(255),
                                  from_account_id UUID NOT NULL,
                                  to_account_id UUID NOT NULL,
                                  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                  updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

                                  CONSTRAINT fk_transaction_logs_from_account
                                      FOREIGN KEY (from_account_id)
                                          REFERENCES accounts(id),

                                  CONSTRAINT fk_transaction_logs_to_account
                                      FOREIGN KEY (to_account_id)
                                          REFERENCES accounts(id)
);

CREATE INDEX idx_transaction_logs_from_account ON transaction_logs(from_account_id);
CREATE INDEX idx_transaction_logs_to_account ON transaction_logs(to_account_id);
CREATE INDEX idx_transaction_logs_reference ON transaction_logs(reference);