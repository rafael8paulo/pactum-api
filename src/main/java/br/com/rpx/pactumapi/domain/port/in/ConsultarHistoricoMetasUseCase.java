package br.com.rpx.pactumapi.domain.port.in;

import br.com.rpx.pactumapi.domain.model.ResultadoMensalMeta;

import java.util.List;
import java.util.UUID;

public interface ConsultarHistoricoMetasUseCase {
    List<ResultadoMensalMeta> consultar(int ano, UUID usuarioId);
}
