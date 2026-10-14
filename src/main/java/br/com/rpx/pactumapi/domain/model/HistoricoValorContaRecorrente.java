package br.com.rpx.pactumapi.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record HistoricoValorContaRecorrente(
        UUID id,
        UUID contaRecorrenteId,
        BigDecimal valor,
        LocalDate vigenteDesde,
        LocalDate vigenteAte
) {}
