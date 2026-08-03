INSERT INTO public.users
(active, id, insured_id, created_at, email, "name", "password", "role") VALUES
    (true, 1, NULL, '2026-07-23 14:11:16.428', 'quitumbaferreira@email.com', 'Quitumba', '$2a$10$NDl52uHKH8k/GqVwx8TIHO0Us41MBXPNGU00v00Y2AeunpzB7Tame', 'ADMIN'),
    (true, 2, 1, '2026-07-23 14:11:16.428', 'joao.silva@email.com', 'João da Silva', '$2a$10$NDl52uHKH8k/GqVwx8TIHO0Us41MBXPNGU00v00Y2AeunpzB7Tame', 'INSURED'),
    (true, 3, 2, '2026-07-23 14:11:16.428', 'maria.oliveira@email.com', 'Maria Oliveira', '$2a$10$NDl52uHKH8k/GqVwx8TIHO0Us41MBXPNGU00v00Y2AeunpzB7Tame', 'INSURED'),
    (true, 4, NULL, '2026-07-23 14:11:16.428', 'carlos.souza@email.com', 'Carlos Souza', '$2a$10$NDl52uHKH8k/GqVwx8TIHO0Us41MBXPNGU00v00Y2AeunpzB7Tame', 'ANALYST');