package br.com.rpx.pactumapi.domain.port.in;

import br.com.rpx.pactumapi.domain.model.ProgressoMeta;

import java.time.YearMonth;
import java.util.UUID;

public interface ConsultarProgressoMetaUseCase {
    ProgressoMeta consultar(YearMonth competencia, UUID usuarioId);
}
