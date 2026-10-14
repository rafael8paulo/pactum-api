package br.com.rpx.pactumapi.domain.port.out;

import br.com.rpx.pactumapi.domain.model.HistoricoValorContaRecorrente;

public interface SalvarHistoricoValorPort {
    HistoricoValorContaRecorrente salvar(HistoricoValorContaRecorrente historicoValor);
}
