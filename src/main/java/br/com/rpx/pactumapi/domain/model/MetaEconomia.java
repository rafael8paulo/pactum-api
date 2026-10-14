package br.com.rpx.pactumapi.domain.model;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

public record MetaEconomia(UUID id, YearMonth competencia, BigDecimal valor, UUID usuarioId) {}
