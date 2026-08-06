package br.com.rpx.pactumapi.domain.port.out;

import br.com.rpx.pactumapi.domain.model.ContaRecorrente;

public interface SalvarContaRecorrentePort {
    ContaRecorrente salvar(ContaRecorrente contaRecorrente);
}
