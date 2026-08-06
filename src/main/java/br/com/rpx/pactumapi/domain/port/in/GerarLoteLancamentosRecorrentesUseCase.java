package br.com.rpx.pactumapi.domain.port.in;

import br.com.rpx.pactumapi.domain.model.Despesa;

import java.util.List;
import java.util.UUID;

public interface GerarLoteLancamentosRecorrentesUseCase {
    List<Despesa> gerarTodos(UUID contaRecorrenteId, UUID usuarioId);
}
