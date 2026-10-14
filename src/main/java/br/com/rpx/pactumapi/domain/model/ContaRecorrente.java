package br.com.rpx.pactumapi.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;
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
        UUID usuarioId,
        FrequenciaCobranca frequencia,
        UUID formaPagamentoId,
        LocalDate dataBaseCobranca
) {
    private static final int ESCALA_MONETARIA = 2;

    public ContaRecorrente {
        if (competenciaFim != null && competenciaFim.isBefore(competenciaInicio)) {
            throw new IllegalArgumentException("competenciaFim não pode ser anterior a competenciaInicio");
        }
        if (dataBaseCobranca != null && competenciaInicio != null
                && !YearMonth.from(dataBaseCobranca).equals(competenciaInicio)) {
            throw new IllegalArgumentException("dataBaseCobranca deve estar dentro do mês de competenciaInicio");
        }
    }

    public boolean cobreCompetencia(YearMonth competencia) {
        boolean apósInicio = !competencia.isBefore(competenciaInicio);
        boolean antesDoFim = competenciaFim == null || !competencia.isAfter(competenciaFim);
        return apósInicio && antesDoFim;
    }

    public Optional<LocalDate> proximaCobranca(LocalDate hoje) {
        if (status != StatusContaRecorrente.ATIVA) {
            return Optional.empty();
        }
        LocalDate data = dataBaseCobranca;
        while (data.isBefore(hoje)) {
            data = avancar(data);
        }
        return Optional.of(data);
    }

    private LocalDate avancar(LocalDate data) {
        return switch (frequencia) {
            case SEMANAL -> data.plusWeeks(1);
            case MENSAL -> data.plusMonths(1);
            case TRIMESTRAL -> data.plusMonths(3);
            case ANUAL -> data.plusYears(1);
        };
    }

    public BigDecimal custoMensal() {
        return switch (frequencia) {
            case MENSAL -> valorPadrao.setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP);
            case TRIMESTRAL -> valorPadrao.divide(BigDecimal.valueOf(3), ESCALA_MONETARIA, RoundingMode.HALF_UP);
            case ANUAL -> valorPadrao.divide(BigDecimal.valueOf(12), ESCALA_MONETARIA, RoundingMode.HALF_UP);
            case SEMANAL -> valorPadrao.multiply(BigDecimal.valueOf(52))
                    .divide(BigDecimal.valueOf(12), ESCALA_MONETARIA, RoundingMode.HALF_UP);
        };
    }
}
