CREATE TABLE policy (
    id BIGSERIAL PRIMARY KEY,
    policy_number VARCHAR(30),
    product_id BIGSERIAL NOT NULL,
    product_code VARCHAR(20) NOT NULL,
    cpf VARCHAR(20) NOT NULL,
    status VARCHAR(20),
    start_date TIMESTAMP NOT NULL DEFAULT now(),
    end_date TIMESTAMP
);