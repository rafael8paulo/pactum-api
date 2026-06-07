package br.com.rpx.pactumapi.domain.port.in;

import br.com.rpx.pactumapi.domain.model.ResumoMensal;

import java.util.List;
import java.util.UUID;

public interface ConsultarHistoricoAnualUseCase {
    List<ResumoMensal> consultar(int ano, UUID usuarioId);
}
