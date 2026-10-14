package br.com.rpx.pactumapi.domain.exception;

import java.time.YearMonth;

public class MetaEconomiaNaoEncontradaException extends RuntimeException {
    public MetaEconomiaNaoEncontradaException(YearMonth competencia) {
        super("Meta de economia não encontrada para a competência: " + competencia);
    }
}
