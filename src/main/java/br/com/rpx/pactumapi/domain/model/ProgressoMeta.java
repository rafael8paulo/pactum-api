package br.com.rpx.pactumapi.domain.model;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

public record ProgressoMeta(
        YearMonth competencia,
        BigDecimal valorMeta,
        BigDecimal valorEconomizado,
        BigDecimal valorRestante,
        BigDecimal percentual,
        int diasRestantes,
        BigDecimal valorDiarioNecessario,
        BigDecimal projecaoFechamento,
        StatusMeta status,
        List<Integer> marcosAtingidos
) {}
