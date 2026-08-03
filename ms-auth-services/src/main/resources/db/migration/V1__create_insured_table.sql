CREATE SEQUENCE insured_id_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE insured (
                         id BIGINT NOT NULL DEFAULT nextval('insured_id_seq'),
                         created_at TIMESTAMP(6),
                         updated_at TIMESTAMP(6),
                         cpf VARCHAR(11),
                         email VARCHAR(150),
                         name VARCHAR(150),
                         phone VARCHAR(20),
                         PRIMARY KEY (id)
);

ALTER SEQUENCE insured_id_seq OWNED BY insured.id;