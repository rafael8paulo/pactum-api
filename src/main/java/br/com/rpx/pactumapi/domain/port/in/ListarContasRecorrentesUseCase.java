package br.com.rpx.pactumapi.domain.port.in;

import br.com.rpx.pactumapi.domain.model.ContaRecorrente;
import br.com.rpx.pactumapi.domain.model.StatusContaRecorrente;

import java.util.List;
import java.util.UUID;

public interface ListarContasRecorrentesUseCase {
    List<ContaRecorrente> listar(StatusContaRecorrente status, UUID usuarioId);
}
