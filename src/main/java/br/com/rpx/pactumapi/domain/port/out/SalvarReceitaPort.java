package br.com.rpx.pactumapi.domain.port.out;

import br.com.rpx.pactumapi.domain.model.Receita;

public interface SalvarReceitaPort {
    Receita salvar(Receita receita);
}
