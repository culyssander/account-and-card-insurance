CREATE SEQUENCE protocol_sequencial_id_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE protocol_sequencial (
                                      id BIGINT PRIMARY KEY NOT NULL DEFAULT nextval('protocol_sequencial_id_seq'),
                                      product_code VARCHAR(10) NOT NULL,
                                      year INTEGER NOT NULL,
                                      last_number INTEGER NOT NULL DEFAULT 0,
                                      created_at TIMESTAMP NOT NULL DEFAULT now(),
                                      CONSTRAINT uk_departamento_ano UNIQUE (product_code, year)
);

ALTER SEQUENCE protocol_sequencial_id_seq OWNED BY protocol_sequencial.id;