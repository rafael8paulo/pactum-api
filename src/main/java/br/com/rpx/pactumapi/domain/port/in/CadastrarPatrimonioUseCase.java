package br.com.rpx.pactumapi.domain.port.in;

import br.com.rpx.pactumapi.domain.model.Patrimonio;

public interface CadastrarPatrimonioUseCase {
    Patrimonio cadastrar(Patrimonio patrimonio);
}
