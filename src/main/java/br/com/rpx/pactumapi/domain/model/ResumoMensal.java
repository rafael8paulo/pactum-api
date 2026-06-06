package br.com.rpx.pactumapi.domain.model;

import java.math.BigDecimal;
import java.time.YearMonth;

public record ResumoMensal(YearMonth competencia, BigDecimal totalReceitas, BigDecimal totalDespesas, BigDecimal saldo) {}
