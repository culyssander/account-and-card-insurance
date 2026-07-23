CREATE TABLE insured (
                          id              BIGSERIAL PRIMARY KEY,
                          name            VARCHAR(150) NOT NULL,
                          cpf             VARCHAR(11)  NOT NULL UNIQUE,
                          email           VARCHAR(150) NOT NULL,
                          phone           VARCHAR(20),
                          created_at      TIMESTAMP NOT NULL DEFAULT now(),
                          updated_at      TIMESTAMP NOT NULL DEFAULT now()
);