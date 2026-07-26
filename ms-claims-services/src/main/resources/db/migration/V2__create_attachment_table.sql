CREATE TABLE attachment (
                       id              BIGSERIAL PRIMARY KEY,
                       claim_id     BIGINT NOT NULL REFERENCES claim(id),
                       file_name    VARCHAR(255) NOT NULL,
                       document_type  VARCHAR(50) NOT NULL,   -- ex: BOLETIM_OCORRENCIA, COMPROVANTE_SAQUE
                       url VARCHAR(500) NOT NULL,
                       size   BIGINT,
                       uploaded_at     TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_attachment_claim ON attachment(claim_id);