package br.com.rpx.pactumapi.domain.model;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

public record Patrimonio(UUID id, String descricao, BigDecimal valor, YearMonth competencia, UUID usuarioId) {}
