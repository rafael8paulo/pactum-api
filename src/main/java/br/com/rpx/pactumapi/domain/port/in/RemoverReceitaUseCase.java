package br.com.rpx.pactumapi.domain.port.in;

import java.util.UUID;

public interface RemoverReceitaUseCase {
    void remover(UUID id, UUID usuarioId);
}
