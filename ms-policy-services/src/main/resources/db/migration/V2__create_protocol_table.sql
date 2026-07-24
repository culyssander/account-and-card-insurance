CREATE TABLE protocol_sequencial (
                                      id BIGSERIAL PRIMARY KEY,
                                      product_code VARCHAR(10) NOT NULL,
                                      year INTEGER NOT NULL,
                                      last_number INTEGER NOT NULL DEFAULT 0,
                                      created_at TIMESTAMP NOT NULL DEFAULT now(),
                                      CONSTRAINT uk_departamento_ano UNIQUE (product_code, year)
);