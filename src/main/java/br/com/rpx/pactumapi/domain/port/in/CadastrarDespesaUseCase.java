package br.com.rpx.pactumapi.domain.port.in;

import br.com.rpx.pactumapi.domain.model.Despesa;

public interface CadastrarDespesaUseCase {
    Despesa cadastrar(Despesa despesa);
}
