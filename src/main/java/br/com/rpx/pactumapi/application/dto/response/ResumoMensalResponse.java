package br.com.rpx.pactumapi.application.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.YearMonth;

public record ResumoMensalResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM") YearMonth competencia,
        BigDecimal totalReceitas,
        BigDecimal totalDespesas,
        BigDecimal saldo
) {}
