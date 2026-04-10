CREATE TABLE idempotency_keys (
                                  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  idempotency_key VARCHAR(255) UNIQUE NOT NULL,
                                  response        JSONB NOT NULL,
                                  http_status     INT NOT NULL,
                                  created_at      TIMESTAMP DEFAULT NOW()
);