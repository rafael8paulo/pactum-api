package br.com.rpx.pactumapi.domain.model;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

public record ContaRecorrente(
        UUID id,
        String descricao,
        BigDecimal valorPadrao,
        CategoriaDespesa categoria,
        Integer diaVencimento,
        YearMonth competenciaInicio,
        YearMonth competenciaFim,
        StatusContaRecorrente status,
        UUID usuarioId
) {
    public ContaRecorrente {
        if (competenciaFim != null && competenciaFim.isBefore(competenciaInicio)) {
            throw new IllegalArgumentException("competenciaFim não pode ser anterior a competenciaInicio");
        }
    }

    public boolean cobreCompetencia(YearMonth competencia) {
        boolean apósInicio = !competencia.isBefore(competenciaInicio);
        boolean antesDoFim = competenciaFim == null || !competencia.isAfter(competenciaFim);
        return apósInicio && antesDoFim;
    }
}
