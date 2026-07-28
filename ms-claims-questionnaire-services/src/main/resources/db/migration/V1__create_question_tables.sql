CREATE TABLE question (
    id   BIGSERIAL PRIMARY KEY,
    code VARCHAR(10)  NOT NULL UNIQUE,
    text VARCHAR(500) NOT NULL,
    type VARCHAR(30)  NOT NULL
);

CREATE TABLE question_option (
    id                 BIGSERIAL PRIMARY KEY,
    question_id        BIGINT       NOT NULL REFERENCES question(id),
    option_code        VARCHAR(60)  NOT NULL,
    label              VARCHAR(255) NOT NULL,
    next_question_code VARCHAR(10),
    outcome_code       VARCHAR(60),
    display_order      INT          NOT NULL,
    CONSTRAINT uq_question_option UNIQUE (question_id, option_code),
    -- mesma regra do construtor compacto de QuestionOption, garantida
    -- também no banco: nunca as duas colunas preenchidas, nunca as duas vazias.
    CONSTRAINT chk_question_option_terminal_xor CHECK (
        (next_question_code IS NOT NULL AND outcome_code IS NULL) OR
        (next_question_code IS NULL AND outcome_code IS NOT NULL)
    )
);

CREATE INDEX idx_question_option_question_id ON question_option (question_id);

CREATE TABLE questionnaire_session (
    id BIGSERIAL PRIMARY KEY,
    claim_id VARCHAR(36),
    current_question_id VARCHAR(36),
    FOREIGN KEY (current_question_id) REFERENCES question(id)
);

CREATE TABLE answered_question (
    id BIGSERIAL PRIMARY KEY,
    questionnaire_id BIGINT NOT NULL,
    claim_id VARCHAR(36) NOT NULL,
    question_id VARCHAR(36) NOT NULL,
    answer TEXT,
    created_at TIMESTAMP DEFAULT now(),
    next_question_id VARCHAR(36),
    FOREIGN KEY (questionnaire_id) REFERENCES questionnaire_session(id),
    FOREIGN KEY (question_id) REFERENCES question(id)
);