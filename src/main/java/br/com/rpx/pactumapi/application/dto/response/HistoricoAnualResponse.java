package br.com.rpx.pactumapi.application.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record HistoricoAnualResponse(
        int ano,
        List<ResumoMensalResponse> meses,
        BigDecimal totalAnualReceitas,
        BigDecimal totalAnualDespesas,
        BigDecimal saldoAnual
) {}
