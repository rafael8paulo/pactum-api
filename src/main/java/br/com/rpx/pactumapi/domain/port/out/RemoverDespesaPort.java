package br.com.rpx.pactumapi.domain.port.out;

import java.util.UUID;

public interface RemoverDespesaPort {
    void remover(UUID id);
}
