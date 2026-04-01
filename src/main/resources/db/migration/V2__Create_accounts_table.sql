CREATE TABLE accounts (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      account_number VARCHAR(20) UNIQUE NOT NULL,
      user_id BIGINT NOT NULL,
      account_type VARCHAR(20) NOT NULL,
      currency VARCHAR(3) NOT NULL DEFAULT 'GBP',
      account_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
      created_at TIMESTAMP NOT NULL DEFAULT NOW(),
      updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
      closed_at TIMESTAMP,

      CONSTRAINT fk_accounts_user
          FOREIGN KEY (user_id)
              REFERENCES users(id)
);

CREATE INDEX idx_accounts_user_id ON accounts(user_id);
CREATE INDEX idx_accounts_account_number ON accounts(account_number);