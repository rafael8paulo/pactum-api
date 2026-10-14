package br.com.rpx.pactumapi.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public record TotalPorFormaPagamento(
        UUID formaPagamentoId,
        String nomeFormaPagamento,
        BigDecimal totalMensal
) {}
