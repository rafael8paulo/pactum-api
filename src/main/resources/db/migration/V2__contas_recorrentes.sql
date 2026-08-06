CREATE TABLE contas_recorrentes (
    id                  UUID           NOT NULL,
    descricao           VARCHAR(100)   NOT NULL,
    valor_padrao        NUMERIC(10, 2) NOT NULL,
    categoria           VARCHAR(50)    NOT NULL,
    dia_vencimento      INTEGER,
    competencia_inicio  DATE           NOT NULL,
    competencia_fim     DATE,
    status              VARCHAR(20)    NOT NULL,
    usuario_id          UUID           NOT NULL,
    created_at          TIMESTAMP      NOT NULL,
    updated_at          TIMESTAMP      NOT NULL,
    CONSTRAINT contas_recorrentes_pkey PRIMARY KEY (id)
);

ALTER TABLE despesas ADD COLUMN conta_recorrente_id UUID;

ALTER TABLE despesas
    ADD CONSTRAINT despesas_conta_recorrente_id_fkey
        FOREIGN KEY (conta_recorrente_id) REFERENCES contas_recorrentes(id) ON DELETE SET NULL;

CREATE INDEX despesas_conta_recorrente_id_idx ON despesas (conta_recorrente_id);
