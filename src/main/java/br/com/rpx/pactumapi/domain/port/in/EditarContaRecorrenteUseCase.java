package br.com.rpx.pactumapi.domain.port.in;

import br.com.rpx.pactumapi.domain.model.ContaRecorrente;

import java.util.UUID;

public interface EditarContaRecorrenteUseCase {
    ContaRecorrente editar(UUID id, ContaRecorrente contaRecorrente, UUID usuarioId);
}
