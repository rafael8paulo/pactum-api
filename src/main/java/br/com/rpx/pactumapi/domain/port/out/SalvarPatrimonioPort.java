package br.com.rpx.pactumapi.domain.port.out;

import br.com.rpx.pactumapi.domain.model.Patrimonio;

public interface SalvarPatrimonioPort {
    Patrimonio salvar(Patrimonio patrimonio);
}
