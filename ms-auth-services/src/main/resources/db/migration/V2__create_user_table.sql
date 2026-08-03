CREATE SEQUENCE users_id_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE users (
                         id              BIGINT NOT NULL DEFAULT nextval('users_id_seq'),
                         name            VARCHAR(150) NOT NULL,
                         email           VARCHAR(150) NOT NULL UNIQUE,
                         password        VARCHAR(150) NOT NULL,
                         active          BOOLEAN NOT NULL DEFAULT TRUE,
                         role            VARCHAR(20) NOT NULL
                             CHECK (role IN ('INSURED', 'ANALYST', 'ADMIN')),
                         insured_id      BIGINT REFERENCES insured(id) DEFAULT NULL,  -- fill if role = INSURED
                         created_at      TIMESTAMP NOT NULL DEFAULT now(),
                         PRIMARY KEY (id)
);

ALTER SEQUENCE users_id_seq OWNED BY users.id;