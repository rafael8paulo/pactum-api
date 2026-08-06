package br.com.rpx.pactumapi.domain.port.out;

import br.com.rpx.pactumapi.domain.model.ContaRecorrente;
import br.com.rpx.pactumapi.domain.model.StatusContaRecorrente;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BuscarContasRecorrentesPort {
    Optional<ContaRecorrente> buscarPorId(UUID id);
    List<ContaRecorrente> buscarPorFiltros(StatusContaRecorrente status, UUID usuarioId);
}
