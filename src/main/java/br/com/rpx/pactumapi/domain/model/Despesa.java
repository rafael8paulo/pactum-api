package br.com.rpx.pactumapi.domain.model;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

public record Despesa(
        UUID id,
        String descricao,
        BigDecimal valor,
        StatusDespesa status,
        YearMonth competencia,
        CategoriaDespesa categoria,
        UUID usuarioId
) {}
