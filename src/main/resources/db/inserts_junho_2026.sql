-- =============================================================
-- Dados de Junho/2026 — gerado a partir de Contas - Junho.csv
-- =============================================================

-- -------------------------------------------------------
-- DESPESAS
-- -------------------------------------------------------
INSERT INTO despesas (id, descricao, valor, status, competencia, categoria, created_at, updated_at) VALUES
    (gen_random_uuid(), 'Financiamento do Carro',   1335.50,    'PENDENTE', '2026-06-01', 'FINANCIAMENTO',  NOW(), NOW()),
    (gen_random_uuid(), 'Fatura do Cartão Itaú',       0.00,    'PAGA',     '2026-06-01', 'CARTAO_CREDITO', NOW(), NOW()),
    (gen_random_uuid(), 'Financiamento Faculdade',   260.09,    'PAGA',     '2026-06-01', 'EDUCACAO',       NOW(), NOW()),
    (gen_random_uuid(), 'Internet (ROS)',              89.90,    'PAGA',     '2026-06-01', 'SERVICOS',       NOW(), NOW()),
    (gen_random_uuid(), 'Celular (Vivo)',              48.96,    'PAGA',     '2026-06-01', 'SERVICOS',       NOW(), NOW()),
    (gen_random_uuid(), 'Fatura Cartão MELI',         169.79,    'PAGA',     '2026-06-01', 'CARTAO_CREDITO', NOW(), NOW()),
    (gen_random_uuid(), 'Fatura Nubank',             3325.92,    'PAGA',     '2026-06-01', 'CARTAO_CREDITO', NOW(), NOW()),
    (gen_random_uuid(), 'Pós UNIPDS',                 499.00,    'PAGA',     '2026-06-01', 'EDUCACAO',       NOW(), NOW()),
    (gen_random_uuid(), 'Futebol',                     50.00,    'PENDENTE', '2026-06-01', 'LAZER',          NOW(), NOW()),
    (gen_random_uuid(), 'Licenciamento Agile',         174.08,    'PAGA',     '2026-06-01', 'SERVICOS',       NOW(), NOW()),
    (gen_random_uuid(), 'C6',                          480.17,    'PAGA',     '2026-06-01', 'CARTAO_CREDITO', NOW(), NOW());

-- -------------------------------------------------------
-- RECEITAS
-- -------------------------------------------------------
INSERT INTO receitas (id, descricao, valor, competencia, categoria, created_at, updated_at) VALUES
    (gen_random_uuid(), 'Salário', 5136.38, '2026-06-01', 'SALARIO', NOW(), NOW()),
    (gen_random_uuid(), 'Ducz',     900.04, '2026-06-01', 'OUTROS',  NOW(), NOW());
