package br.com.rpx.pactumapi.application.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record TotalPorFormaPagamentoResponse(
        UUID formaPagamentoId,
        String nomeFormaPagamento,
        BigDecimal totalMensal
) {}
