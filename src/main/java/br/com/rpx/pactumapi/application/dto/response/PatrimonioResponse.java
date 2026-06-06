package br.com.rpx.pactumapi.application.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

public record PatrimonioResponse(
        UUID id,
        String descricao,
        BigDecimal valor,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM") YearMonth competencia
) {}
