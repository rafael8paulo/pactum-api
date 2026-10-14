package br.com.rpx.pactumapi.domain.port.out;

import java.util.UUID;

public interface RemoverFormaPagamentoPort {
    void remover(UUID id);
}
