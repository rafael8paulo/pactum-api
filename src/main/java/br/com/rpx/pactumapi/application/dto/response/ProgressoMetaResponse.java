package br.com.rpx.pactumapi.application.dto.response;

import br.com.rpx.pactumapi.domain.model.StatusMeta;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

public record ProgressoMetaResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM") YearMonth competencia,
        BigDecimal valorMeta,
        BigDecimal valorEconomizado,
        BigDecimal valorRestante,
        BigDecimal percentual,
        int diasRestantes,
        BigDecimal valorDiarioNecessario,
        BigDecimal projecaoFechamento,
        StatusMeta status,
        List<Integer> marcosAtingidos
) {}
