package br.com.rpx.pactumapi.domain.port.in;

import java.util.UUID;

public interface RemoverFormaPagamentoUseCase {
    void remover(UUID id, UUID usuarioId);
}
