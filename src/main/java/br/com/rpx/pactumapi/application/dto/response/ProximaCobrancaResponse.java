package br.com.rpx.pactumapi.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ProximaCobrancaResponse(
        UUID contaRecorrenteId,
        String descricao,
        BigDecimal valorPadrao,
        UUID formaPagamentoId,
        LocalDate proximaCobranca
) {}
