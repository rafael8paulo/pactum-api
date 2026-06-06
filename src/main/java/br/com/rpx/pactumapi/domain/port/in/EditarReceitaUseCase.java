package br.com.rpx.pactumapi.domain.port.in;

import br.com.rpx.pactumapi.domain.model.Receita;

import java.util.UUID;

public interface EditarReceitaUseCase {
    Receita editar(UUID id, Receita receita);
}
