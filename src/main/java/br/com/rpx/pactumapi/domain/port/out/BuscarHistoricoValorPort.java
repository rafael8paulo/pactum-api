package br.com.rpx.pactumapi.domain.port.out;

import br.com.rpx.pactumapi.domain.model.HistoricoValorContaRecorrente;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BuscarHistoricoValorPort {
    Optional<HistoricoValorContaRecorrente> buscarVigente(UUID contaRecorrenteId);
    List<HistoricoValorContaRecorrente> buscarPorContaRecorrenteOrdenado(UUID contaRecorrenteId);
}
