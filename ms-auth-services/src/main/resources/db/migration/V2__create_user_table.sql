CREATE TABLE user_ (
                         id              BIGSERIAL PRIMARY KEY,
                         name            VARCHAR(150) NOT NULL,
                         email           VARCHAR(150) NOT NULL UNIQUE,
                         password        VARCHAR(150) NOT NULL,
                         active          BOOLEAN NOT NULL DEFAULT TRUE,
                         role            VARCHAR(20) NOT NULL
                             CHECK (role IN ('INSURED', 'ANALYST', 'ADMIN')),
                         insured_id      BIGINT REFERENCES insured(id) DEFAULT NULL,  -- fill if role = INSURED
                         created_at      TIMESTAMP NOT NULL DEFAULT now()
);