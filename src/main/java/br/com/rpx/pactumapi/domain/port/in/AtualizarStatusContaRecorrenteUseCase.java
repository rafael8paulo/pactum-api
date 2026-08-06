package br.com.rpx.pactumapi.domain.port.in;

import br.com.rpx.pactumapi.domain.model.ContaRecorrente;
import br.com.rpx.pactumapi.domain.model.StatusContaRecorrente;

import java.util.UUID;

public interface AtualizarStatusContaRecorrenteUseCase {
    ContaRecorrente atualizar(UUID id, StatusContaRecorrente status, UUID usuarioId);
}
