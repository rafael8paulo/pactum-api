package br.com.rpx.pactumapi.domain.port.in;

import br.com.rpx.pactumapi.domain.model.Receita;

public interface CadastrarReceitaUseCase {
    Receita cadastrar(Receita receita);
}
