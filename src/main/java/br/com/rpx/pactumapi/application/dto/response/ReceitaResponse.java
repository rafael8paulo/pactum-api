package br.com.rpx.pactumapi.application.dto.response;

import br.com.rpx.pactumapi.domain.model.CategoriaReceita;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

public record ReceitaResponse(
        UUID id,
        String descricao,
        BigDecimal valor,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM") YearMonth competencia,
        CategoriaReceita categoria
) {}
