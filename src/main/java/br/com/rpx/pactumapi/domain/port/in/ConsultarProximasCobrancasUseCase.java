package br.com.rpx.pactumapi.domain.port.in;

import br.com.rpx.pactumapi.domain.model.ProximaCobranca;

import java.util.List;
import java.util.UUID;

public interface ConsultarProximasCobrancasUseCase {
    List<ProximaCobranca> consultar(int dias, UUID usuarioId);
}
