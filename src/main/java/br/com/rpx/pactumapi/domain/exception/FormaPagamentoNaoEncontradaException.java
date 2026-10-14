package br.com.rpx.pactumapi.domain.exception;

import java.util.UUID;

public class FormaPagamentoNaoEncontradaException extends RuntimeException {
    public FormaPagamentoNaoEncontradaException(UUID id) {
        super("Forma de pagamento não encontrada: " + id);
    }
}
