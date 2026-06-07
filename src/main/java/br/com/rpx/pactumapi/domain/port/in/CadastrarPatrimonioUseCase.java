package br.com.rpx.pactumapi.domain.port.in;

import br.com.rpx.pactumapi.domain.model.Patrimonio;

import java.util.UUID;

public interface CadastrarPatrimonioUseCase {
    Patrimonio cadastrar(Patrimonio patrimonio, UUID usuarioId);
}
