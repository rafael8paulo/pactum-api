package br.com.rpx.pactumapi.domain.port.in;

import br.com.rpx.pactumapi.domain.model.ResumoContasRecorrentes;

import java.util.UUID;

public interface ConsultarResumoContasRecorrentesUseCase {
    ResumoContasRecorrentes consultar(UUID usuarioId);
}
