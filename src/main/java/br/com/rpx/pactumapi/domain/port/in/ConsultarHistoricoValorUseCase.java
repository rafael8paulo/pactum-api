package br.com.rpx.pactumapi.domain.port.in;

import br.com.rpx.pactumapi.domain.model.HistoricoValorContaRecorrente;

import java.util.List;
import java.util.UUID;

public interface ConsultarHistoricoValorUseCase {
    List<HistoricoValorContaRecorrente> consultar(UUID contaRecorrenteId, UUID usuarioId);
}
