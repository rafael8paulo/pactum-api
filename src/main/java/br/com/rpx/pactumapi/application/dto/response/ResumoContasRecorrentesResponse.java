package br.com.rpx.pactumapi.application.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record ResumoContasRecorrentesResponse(
        BigDecimal totalMensalRecorrente,
        List<TotalPorFormaPagamentoResponse> porFormaPagamento
) {}
