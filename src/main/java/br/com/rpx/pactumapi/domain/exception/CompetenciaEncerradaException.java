package br.com.rpx.pactumapi.domain.exception;

import java.time.YearMonth;

public class CompetenciaEncerradaException extends RuntimeException {
    public CompetenciaEncerradaException(YearMonth competencia) {
        super("Competência encerrada, a meta não pode ser alterada: " + competencia);
    }
}
