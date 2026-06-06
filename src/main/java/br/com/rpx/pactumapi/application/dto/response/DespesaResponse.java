package br.com.rpx.pactumapi.application.dto.response;

import br.com.rpx.pactumapi.domain.model.CategoriaDespesa;
import br.com.rpx.pactumapi.domain.model.StatusDespesa;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

public record DespesaResponse(
        UUID id,
        String descricao,
        BigDecimal valor,
        StatusDespesa status,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM") YearMonth competencia,
        CategoriaDespesa categoria
) {}
