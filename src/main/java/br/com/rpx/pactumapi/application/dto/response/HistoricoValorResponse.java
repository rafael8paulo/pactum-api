package br.com.rpx.pactumapi.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record HistoricoValorResponse(
        BigDecimal valor,
        LocalDate vigenteDesde,
        LocalDate vigenteAte
) {}
