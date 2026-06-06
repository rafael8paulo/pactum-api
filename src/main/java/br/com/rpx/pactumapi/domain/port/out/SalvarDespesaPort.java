package br.com.rpx.pactumapi.domain.port.out;

import br.com.rpx.pactumapi.domain.model.Despesa;

public interface SalvarDespesaPort {
    Despesa salvar(Despesa despesa);
}
