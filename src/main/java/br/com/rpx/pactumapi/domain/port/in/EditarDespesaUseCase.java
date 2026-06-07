package br.com.rpx.pactumapi.domain.port.in;

import br.com.rpx.pactumapi.domain.model.Despesa;

import java.util.UUID;

public interface EditarDespesaUseCase {
    Despesa editar(UUID id, Despesa despesa, UUID usuarioId);
}
