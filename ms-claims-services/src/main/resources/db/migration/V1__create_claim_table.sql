CREATE TABLE claim (
                          id                  BIGSERIAL PRIMARY KEY,
                          policy_id           BIGINT NOT NULL,
                          claim_number        VARCHAR(30) NOT NULL UNIQUE,
                          status              VARCHAR(30) NOT NULL DEFAULT 'OPEN'
                              CHECK (status IN (
                                                'OPEN',
                                                'DOCUMENTATION_PENDING',
                                                'UNDER_REVIEW',
                                                'APPROVED',
                                                'DENIED'
                                  )),
                          event_date          TIMESTAMP NOT NULL,
                          opening_date        TIMESTAMP NOT NULL DEFAULT now(),
                          description         TEXT,
                          claimed_amount      NUMERIC(12,2),
                          created_at          TIMESTAMP NOT NULL DEFAULT now(),
                          updated_at          TIMESTAMP NOT NULL DEFAULT now()
);
