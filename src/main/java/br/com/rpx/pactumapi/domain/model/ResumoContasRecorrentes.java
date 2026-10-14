package br.com.rpx.pactumapi.domain.model;

import java.math.BigDecimal;
import java.util.List;

public record ResumoContasRecorrentes(
        BigDecimal totalMensalRecorrente,
        List<TotalPorFormaPagamento> porFormaPagamento
) {}
