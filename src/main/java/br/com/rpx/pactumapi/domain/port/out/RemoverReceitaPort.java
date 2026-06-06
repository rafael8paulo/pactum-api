package br.com.rpx.pactumapi.domain.port.out;

import java.util.UUID;

public interface RemoverReceitaPort {
    void remover(UUID id);
}
