package br.com.rpx.pactumapi.domain.port.in;

import br.com.rpx.pactumapi.domain.model.Despesa;
import br.com.rpx.pactumapi.domain.model.StatusDespesa;

import java.util.UUID;

public interface AtualizarStatusDespesaUseCase {
    Despesa atualizar(UUID id, StatusDespesa status, UUID usuarioId);
}
