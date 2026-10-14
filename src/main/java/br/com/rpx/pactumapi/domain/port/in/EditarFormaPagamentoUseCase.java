package br.com.rpx.pactumapi.domain.port.in;

import br.com.rpx.pactumapi.domain.model.FormaPagamento;

import java.util.UUID;

public interface EditarFormaPagamentoUseCase {
    FormaPagamento editar(UUID id, FormaPagamento formaPagamento, UUID usuarioId);
}
