package br.com.rpx.pactumapi.domain.exception;

import java.util.UUID;

public class ContaRecorrenteNaoEncontradaException extends RuntimeException {
    public ContaRecorrenteNaoEncontradaException(UUID id) {
        super("Conta recorrente não encontrada: " + id);
    }
}
