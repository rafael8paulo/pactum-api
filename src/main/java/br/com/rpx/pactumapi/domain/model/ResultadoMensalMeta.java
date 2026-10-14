package br.com.rpx.pactumapi.domain.model;

import java.math.BigDecimal;
import java.time.YearMonth;

public record ResultadoMensalMeta(
        YearMonth competencia,
        BigDecimal valorMeta,
        BigDecimal valorEconomizado,
        BigDecimal percentual,
        boolean atingida
) {}
