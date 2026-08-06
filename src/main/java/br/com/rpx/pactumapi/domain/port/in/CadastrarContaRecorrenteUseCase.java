package br.com.rpx.pactumapi.domain.port.in;

import br.com.rpx.pactumapi.domain.model.ContaRecorrente;

import java.util.UUID;

public interface CadastrarContaRecorrenteUseCase {
    ContaRecorrente cadastrar(ContaRecorrente contaRecorrente, UUID usuarioId);
}
