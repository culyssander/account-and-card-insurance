-- ---------------------------------------------------------------------
-- ANALISE (resultado emitido pela seguradora)
-- ---------------------------------------------------------------------
CREATE TABLE analysis (
                         id                  BIGSERIAL PRIMARY KEY,
                         claim_id            BIGINT NOT NULL UNIQUE,
                         analyst_id          BIGINT,
                         result              VARCHAR(20) NOT NULL
                             CHECK (result IN ('APPROVED', 'DENIED')),
                         reason_for_denial   TEXT,               -- obrigatório quando resultado = NEGADO
                         compensation_amount NUMERIC(12,2),
                         analysis_date       TIMESTAMP NOT NULL DEFAULT now(),
                         created_at          TIMESTAMP NOT NULL DEFAULT now()
);