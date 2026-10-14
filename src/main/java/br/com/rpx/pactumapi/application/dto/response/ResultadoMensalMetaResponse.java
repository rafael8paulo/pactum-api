package br.com.rpx.pactumapi.application.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.YearMonth;

public record ResultadoMensalMetaResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM") YearMonth competencia,
        BigDecimal valorMeta,
        BigDecimal valorEconomizado,
        BigDecimal percentual,
        boolean atingida
) {}
