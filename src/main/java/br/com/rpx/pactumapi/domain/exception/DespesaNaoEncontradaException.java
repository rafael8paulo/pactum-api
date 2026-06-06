package br.com.rpx.pactumapi.domain.exception;

import java.util.UUID;

public class DespesaNaoEncontradaException extends RuntimeException {
    public DespesaNaoEncontradaException(UUID id) {
        super("Despesa não encontrada: " + id);
    }
}
