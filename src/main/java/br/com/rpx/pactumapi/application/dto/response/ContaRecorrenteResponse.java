package br.com.rpx.pactumapi.application.dto.response;

import br.com.rpx.pactumapi.domain.model.CategoriaDespesa;
import br.com.rpx.pactumapi.domain.model.StatusContaRecorrente;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

public record ContaRecorrenteResponse(
        UUID id,
        String descricao,
        BigDecimal valorPadrao,
        CategoriaDespesa categoria,
        Integer diaVencimento,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM") YearMonth competenciaInicio,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM") YearMonth competenciaFim,
        StatusContaRecorrente status
) {}
