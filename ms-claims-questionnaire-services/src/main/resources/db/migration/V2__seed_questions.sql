-- Nível 1
INSERT INTO question (code, text, type) VALUES
    ('Q001', 'O que aconteceu com o cliente?', 'SINGLE_CHOICE');

-- Nível 2
INSERT INTO question (code, text, type) VALUES
    ('Q002', 'O cartão foi bloqueado?', 'SINGLE_CHOICE'),
    ('Q003', 'O cliente foi obrigado a fazer Pix, saque ou compra?', 'SINGLE_CHOICE'),
    ('Q004', 'O dinheiro foi roubado até 8 horas após o saque?', 'SINGLE_CHOICE'),
    ('Q005', 'A bolsa/mochila/carteira foi roubada junto com o cartão?', 'SINGLE_CHOICE');

-- Opções de Q001 — cada uma abre uma sub-árvore de nível 2
INSERT INTO question_option (question_id, option_code, label, next_question_code, outcome_code, display_order)
SELECT id, 'CARD_THEFT', 'Roubo/Furto do cartão', 'Q002', NULL, 1 FROM question WHERE code = 'Q001';
INSERT INTO question_option (question_id, option_code, label, next_question_code, outcome_code, display_order)
SELECT id, 'COERCION_TRANSACTION', 'Transação sob coação', 'Q003', NULL, 2 FROM question WHERE code = 'Q001';
INSERT INTO question_option (question_id, option_code, label, next_question_code, outcome_code, display_order)
SELECT id, 'CASH_WITHDRAWAL_ROBBERY', 'Roubo após saque', 'Q004', NULL, 3 FROM question WHERE code = 'Q001';
INSERT INTO question_option (question_id, option_code, label, next_question_code, outcome_code, display_order)
SELECT id, 'PERSONAL_ITEMS_THEFT', 'Roubo de bens pessoais', 'Q005', NULL, 4 FROM question WHERE code = 'Q001';

-- Q002 (Roubo/Furto do cartão) — Sim/Não convergem na mesma ação
INSERT INTO question_option (question_id, option_code, label, next_question_code, outcome_code, display_order)
SELECT id, 'YES', 'Sim', NULL, 'REQUEST_DOCUMENTS_CARD_THEFT', 1 FROM question WHERE code = 'Q002';
INSERT INTO question_option (question_id, option_code, label, next_question_code, outcome_code, display_order)
SELECT id, 'NO', 'Não', NULL, 'REQUEST_DOCUMENTS_CARD_THEFT', 2 FROM question WHERE code = 'Q002';

-- Q003 (Transação sob coação) — Sim/Não convergem em avaliação de apólice
INSERT INTO question_option (question_id, option_code, label, next_question_code, outcome_code, display_order)
SELECT id, 'YES', 'Sim', NULL, 'EVALUATE_POLICY_COVERAGE', 1 FROM question WHERE code = 'Q003';
INSERT INTO question_option (question_id, option_code, label, next_question_code, outcome_code, display_order)
SELECT id, 'NO', 'Não', NULL, 'EVALUATE_POLICY_COVERAGE', 2 FROM question WHERE code = 'Q003';

-- Q004 (Roubo após saque) — Sim/Não convergem na mesma ação
INSERT INTO question_option (question_id, option_code, label, next_question_code, outcome_code, display_order)
SELECT id, 'YES', 'Sim', NULL, 'REQUEST_DOCUMENTS_CASH_ROBBERY', 1 FROM question WHERE code = 'Q004';
INSERT INTO question_option (question_id, option_code, label, next_question_code, outcome_code, display_order)
SELECT id, 'NO', 'Não', NULL, 'REQUEST_DOCUMENTS_CASH_ROBBERY', 2 FROM question WHERE code = 'Q004';

-- Q005 (Roubo de bens pessoais) — Sim/Não convergem em verificação de bens cobertos
INSERT INTO question_option (question_id, option_code, label, next_question_code, outcome_code, display_order)
SELECT id, 'YES', 'Sim', NULL, 'VERIFY_COVERED_ITEMS', 1 FROM question WHERE code = 'Q005';
INSERT INTO question_option (question_id, option_code, label, next_question_code, outcome_code, display_order)
SELECT id, 'NO', 'Não', NULL, 'VERIFY_COVERED_ITEMS', 2 FROM question WHERE code = 'Q005';
