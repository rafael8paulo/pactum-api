package br.com.rpx.pactumapi.domain.service;

import br.com.rpx.pactumapi.domain.model.ContaRecorrente;

import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

class ElegibilidadeCompetencia {

    private ElegibilidadeCompetencia() {}

    static boolean eligivel(ContaRecorrente conta, YearMonth competencia) {
        long mesesDesdeInicio = ChronoUnit.MONTHS.between(conta.competenciaInicio(), competencia);
        return switch (conta.frequencia()) {
            case MENSAL -> true;
            case TRIMESTRAL -> mesesDesdeInicio % 3 == 0;
            case ANUAL -> mesesDesdeInicio % 12 == 0;
            case SEMANAL -> false;
        };
    }
}
