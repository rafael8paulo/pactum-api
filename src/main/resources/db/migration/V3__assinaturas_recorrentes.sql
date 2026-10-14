CREATE TABLE formas_pagamento (
    id                      UUID           NOT NULL,
    nome                    VARCHAR(100)   NOT NULL,
    tipo                    VARCHAR(30)    NOT NULL,
    dia_fechamento_fatura   INTEGER,
    usuario_id              UUID           NOT NULL,
    created_at              TIMESTAMP      NOT NULL,
    updated_at              TIMESTAMP      NOT NULL,
    CONSTRAINT formas_pagamento_pkey PRIMARY KEY (id)
);

ALTER TABLE contas_recorrentes ADD COLUMN frequencia VARCHAR(20) NOT NULL DEFAULT 'MENSAL';

ALTER TABLE contas_recorrentes ADD COLUMN forma_pagamento_id UUID;

ALTER TABLE contas_recorrentes
    ADD CONSTRAINT contas_recorrentes_forma_pagamento_id_fkey
        FOREIGN KEY (forma_pagamento_id) REFERENCES formas_pagamento(id) ON DELETE SET NULL;

CREATE INDEX contas_recorrentes_forma_pagamento_id_idx ON contas_recorrentes (forma_pagamento_id);

ALTER TABLE contas_recorrentes ADD COLUMN data_base_cobranca DATE;

-- Backfill: usa o primeiro dia de competencia_inicio deslocado pelo dia_vencimento
-- (limitado a 28 para evitar estourar o mês), já que contas existentes eram mensais.
UPDATE contas_recorrentes
    SET data_base_cobranca = competencia_inicio + (LEAST(COALESCE(dia_vencimento, 1), 28) - 1)
    WHERE data_base_cobranca IS NULL;

ALTER TABLE contas_recorrentes ALTER COLUMN data_base_cobranca SET NOT NULL;

CREATE TABLE historico_valor_conta_recorrente (
    id                    UUID           NOT NULL,
    conta_recorrente_id   UUID           NOT NULL,
    valor                 NUMERIC(10, 2) NOT NULL,
    vigente_desde         DATE           NOT NULL,
    vigente_ate           DATE,
    created_at            TIMESTAMP      NOT NULL,
    updated_at            TIMESTAMP      NOT NULL,
    CONSTRAINT historico_valor_conta_recorrente_pkey PRIMARY KEY (id),
    CONSTRAINT historico_valor_conta_recorrente_conta_fkey
        FOREIGN KEY (conta_recorrente_id) REFERENCES contas_recorrentes(id) ON DELETE CASCADE
);

CREATE INDEX historico_valor_conta_recorrente_conta_idx ON historico_valor_conta_recorrente (conta_recorrente_id);

-- Backfill: um registro de histórico "vigente desde o cadastro" por conta recorrente já existente.
-- Reaproveita o id da própria conta recorrente como id do registro de histórico (1:1 nesse backfill,
-- então não há colisão), evitando depender de uma função de geração de UUID específica do banco.
INSERT INTO historico_valor_conta_recorrente (id, conta_recorrente_id, valor, vigente_desde, vigente_ate, created_at, updated_at)
SELECT id, id, valor_padrao, competencia_inicio, NULL, now(), now()
FROM contas_recorrentes;
