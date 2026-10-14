CREATE TABLE metas_economia (
    id           UUID           NOT NULL,
    usuario_id   UUID           NOT NULL,
    competencia  DATE           NOT NULL,
    valor        NUMERIC(10, 2) NOT NULL,
    created_at   TIMESTAMP      NOT NULL,
    updated_at   TIMESTAMP      NOT NULL,
    CONSTRAINT metas_economia_pkey PRIMARY KEY (id),
    CONSTRAINT metas_economia_usuario_competencia_uk UNIQUE (usuario_id, competencia)
);
